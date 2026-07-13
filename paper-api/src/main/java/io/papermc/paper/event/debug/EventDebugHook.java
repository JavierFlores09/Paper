package io.papermc.paper.event.debug;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Observes events as they are dispatched to registered listeners.
 * <p>
 * Hooks are invoked on the thread dispatching the event and should not modify
 * the event or perform expensive work. Exceptions thrown by a hook are logged
 * and do not affect event dispatch.
 * <p>
 * Events which are not created because their call site checks for registered
 * listeners cannot be observed by this hook.
 */
@ApiStatus.Experimental
public interface EventDebugHook {

    /**
     * Called before an event is dispatched to its listeners.
     *
     * @param event the event being dispatched
     */
    default void onEventStart(final @NotNull Event event) {
    }

    /**
     * Called immediately before a registered listener is invoked.
     *
     * @param event the event being dispatched
     * @param listener the listener about to be invoked
     */
    default void onListenerStart(final @NotNull Event event, final @NotNull RegisteredListener listener) {
    }

    /**
     * Invokes a registered listener.
     * <p>
     * Hooks may override this method to establish context around a listener
     * invocation. The supplied invocation must be called exactly once.
     *
     * @param event the event being dispatched
     * @param listener the listener being invoked
     * @param invocation the listener invocation
     * @throws EventException if the listener throws an event exception
     */
    default void onListenerInvoke(
        final @NotNull Event event,
        final @NotNull RegisteredListener listener,
        final @NotNull ListenerInvocation invocation
    ) throws EventException {
        invocation.invoke();
    }

    /**
     * Called after a registered listener was invoked, even if it threw.
     *
     * @param event the event being dispatched
     * @param listener the listener which was invoked
     * @param throwable the throwable from the listener, or {@code null}
     */
    default void onListenerEnd(final @NotNull Event event, final @NotNull RegisteredListener listener, final @Nullable Throwable throwable) {
    }

    /**
     * Called after an event has finished dispatching to its listeners.
     *
     * @param event the event which was dispatched
     */
    default void onEventEnd(final @NotNull Event event) {
    }

    /**
     * Invokes an event listener.
     */
    @FunctionalInterface
    interface ListenerInvocation {

        /**
         * Invokes the listener.
         *
         * @throws EventException if the listener throws an event exception
         */
        void invoke() throws EventException;
    }
}
