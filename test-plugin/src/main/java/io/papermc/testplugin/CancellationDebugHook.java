package io.papermc.testplugin;

import io.papermc.paper.event.debug.EventDebugHook;
import java.util.logging.Logger;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.plugin.RegisteredListener;

final class CancellationDebugHook implements EventDebugHook {

    private static final ScopedValue<Boolean> CANCELLED_BEFORE = ScopedValue.newInstance();

    private final Logger logger;

    CancellationDebugHook(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onListenerInvoke(
        final Event event,
        final RegisteredListener listener,
        final ListenerInvocation invocation
    ) throws EventException {
        if (!(event instanceof Cancellable cancellable)) {
            invocation.invoke();
            return;
        }

        ScopedValue.where(CANCELLED_BEFORE, cancellable.isCancelled()).call(() -> {
            try {
                invocation.invoke();
            } finally {
                final boolean cancelledAfter = cancellable.isCancelled();
                if (CANCELLED_BEFORE.get() != cancelledAfter) {
                    this.cancellationChanged(event, listener, CANCELLED_BEFORE.get(), cancelledAfter);
                }
            }
            return null;
        });
    }

    private void cancellationChanged(
        final Event event,
        final RegisteredListener listener,
        final boolean cancelledBefore,
        final boolean cancelledAfter
    ) {
        this.logger.info(
            listener.getPlugin().getName() + " changed " + event.getEventName()
                + " cancellation state from " + cancelledBefore + " to " + cancelledAfter
        );
    }
}
