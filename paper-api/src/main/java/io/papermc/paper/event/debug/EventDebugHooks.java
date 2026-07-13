package io.papermc.paper.event.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registry for event debug hooks.
 */
@ApiStatus.Experimental
public final class EventDebugHooks {

    private static final Logger LOGGER = Logger.getLogger(EventDebugHooks.class.getName());
    private static final Lock HOOKS_LOCK = new ReentrantLock();
    private static final List<EventDebugHook> HOOKS = new ArrayList<>();
    private static volatile EventDebugHook currentHook;

    private EventDebugHooks() {
    }

    /**
     * Registers an event debug hook.
     *
     * @param hook the hook to register
     * @return a registration which unregisters the hook when closed
     */
    public static @NotNull Registration register(final @NotNull EventDebugHook hook) {
        Objects.requireNonNull(hook, "hook");
        HOOKS_LOCK.lock();
        try {
            HOOKS.add(hook);
            rebuildCurrentHook();
        } finally {
            HOOKS_LOCK.unlock();
        }

        final AtomicBoolean registered = new AtomicBoolean(true);
        return () -> {
            if (registered.compareAndSet(true, false)) {
                HOOKS_LOCK.lock();
                try {
                    HOOKS.remove(hook);
                    rebuildCurrentHook();
                } finally {
                    HOOKS_LOCK.unlock();
                }
            }
        };
    }

    private static void rebuildCurrentHook() {
        currentHook = HOOKS.isEmpty() ? null : new SafeEventDebugHook(HOOKS.toArray(EventDebugHook[]::new));
        if (Bukkit.getPluginManager() instanceof HookHandler handler) {
            handler.update(currentHook);
        }
    }

    private record SafeEventDebugHook(EventDebugHook[] hooks) implements EventDebugHook {

        @Override
        public void onEventStart(final Event event) {
            for (final EventDebugHook hook : this.hooks) {
                try {
                    hook.onEventStart(event);
                } catch (final Throwable throwable) {
                    reportFailure(hook, throwable);
                }
            }
        }

        @Override
        public void onListenerStart(final Event event, final RegisteredListener listener) {
            for (final EventDebugHook hook : this.hooks) {
                try {
                    hook.onListenerStart(event, listener);
                } catch (final Throwable throwable) {
                    reportFailure(hook, throwable);
                }
            }
        }

        @Override
        public void onListenerInvoke(final Event event, final RegisteredListener listener, final ListenerInvocation invocation) throws EventException {
            this.invokeHook(0, event, listener, invocation);
        }

        private void invokeHook(
            final int index,
            final Event event,
            final RegisteredListener listener,
            final ListenerInvocation invocation
        ) throws EventException {
            if (index == this.hooks.length) {
                invocation.invoke();
                return;
            }

            final EventDebugHook hook = this.hooks[index];
            final InvocationState state = new InvocationState();
            try {
                hook.onListenerInvoke(event, listener, () -> {
                    if (state.invoked) {
                        throw new IllegalStateException("Event debug hook attempted to invoke a listener more than once");
                    }
                    state.invoked = true;
                    try {
                        this.invokeHook(index + 1, event, listener, invocation);
                    } catch (final EventException | RuntimeException | Error throwable) {
                        state.listenerThrowable = throwable;
                        throw throwable;
                    }
                });
            } catch (final EventException | RuntimeException | Error throwable) {
                if (state.listenerThrowable != null) {
                    rethrowListenerThrowable(state.listenerThrowable);
                }
                reportFailure(hook, throwable);
            }

            if (state.listenerThrowable != null) {
                rethrowListenerThrowable(state.listenerThrowable);
            } else if (!state.invoked) {
                this.invokeHook(index + 1, event, listener, invocation);
            }
        }

        @Override
        public void onListenerEnd(final Event event, final RegisteredListener listener, final Throwable listenerThrowable) {
            for (final EventDebugHook hook : this.hooks) {
                try {
                    hook.onListenerEnd(event, listener, listenerThrowable);
                } catch (final Throwable throwable) {
                    reportFailure(hook, throwable);
                }
            }
        }

        @Override
        public void onEventEnd(final Event event) {
            for (final EventDebugHook hook : this.hooks) {
                try {
                    hook.onEventEnd(event);
                } catch (final Throwable throwable) {
                    reportFailure(hook, throwable);
                }
            }
        }

        private static void reportFailure(final EventDebugHook hook, final Throwable throwable) {
            LOGGER.log(Level.SEVERE, "Event debug hook " + hook.getClass().getName() + " failed", throwable);
        }

        private static void rethrowListenerThrowable(final Throwable throwable) throws EventException {
            if (throwable instanceof EventException eventException) {
                throw eventException;
            } else if (throwable instanceof RuntimeException runtimeException) {
                throw runtimeException;
            } else {
                throw (Error) throwable;
            }
        }

        private static final class InvocationState {

            private boolean invoked;
            private Throwable listenerThrowable;
        }
    }

    /**
     * A removable event debug hook registration.
     */
    @FunctionalInterface
    public interface Registration extends AutoCloseable {

        /**
         * Unregisters the associated hook. Calling this more than once has no effect.
         */
        @Override
        void close();
    }

    @ApiStatus.Internal
    @FunctionalInterface
    public interface HookHandler {

        void update(@Nullable EventDebugHook hook);
    }
}
