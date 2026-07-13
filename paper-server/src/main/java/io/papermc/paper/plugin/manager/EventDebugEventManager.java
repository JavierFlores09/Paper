package io.papermc.paper.plugin.manager;

import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import com.destroystokyo.paper.exception.ServerEventException;
import io.papermc.paper.event.debug.EventDebugHook;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

final class EventDebugEventManager extends DelegatingEventManager {

    private final EventDebugHook debugHook;

    EventDebugEventManager(final Server server, final PaperEventManager delegate, final EventDebugHook debugHook) {
        super(server, delegate);
        this.debugHook = debugHook;
    }

    @Override
    public void callEvent(final @NotNull Event event) {
        final RegisteredListener[] listeners = getRegisteredListeners(event);

        this.debugHook.onEventStart(event);
        try {
            for (final RegisteredListener registration : listeners) {
                if (!registration.getPlugin().isEnabled()) {
                    continue;
                }
                if (event instanceof Cancellable cancellable && cancellable.isCancelled() && registration.isIgnoringCancelled()) {
                    continue;
                }

                Throwable listenerThrowable = null;
                this.debugHook.onListenerStart(event, registration);
                try {
                    this.debugHook.onListenerInvoke(event, registration, () -> registration.callEvent(event));
                } catch (final Throwable throwable) {
                    listenerThrowable = throwable;
                } finally {
                    this.debugHook.onListenerEnd(event, registration, listenerThrowable);
                }

                if (listenerThrowable instanceof AuthorNagException authorNagException) {
                    this.handleAuthorNag(registration, authorNagException);
                } else if (listenerThrowable != null) {
                    this.handleListenerFailure(event, registration, listenerThrowable);
                }
            }
        } finally {
            this.debugHook.onEventEnd(event);
        }
    }

    private RegisteredListener @NonNull [] getRegisteredListeners(@NonNull Event event) {
        if (event.isAsynchronous() && this.server.isPrimaryThread()) {
            throw new IllegalStateException(event.getEventName() + " may only be triggered asynchronously.");
        } else if (!event.isAsynchronous() && !this.server.isPrimaryThread() && !this.server.isStopping()) {
            throw new IllegalStateException(event.getEventName() + " may only be triggered synchronously.");
        }

        final HandlerList handlers = event.getHandlers();
        return handlers.getRegisteredListeners();
    }

    private void handleAuthorNag(final RegisteredListener registration, final AuthorNagException exception) {
        final Plugin plugin = registration.getPlugin();
        if (plugin.isNaggable()) {
            plugin.setNaggable(false);
            this.server.getLogger().log(Level.SEVERE, String.format(
                "Nag author(s): '%s' of '%s' about the following: %s",
                plugin.getPluginMeta().getAuthors(),
                plugin.getPluginMeta().getDisplayName(),
                exception.getMessage()
            ));
        }
    }

    private void handleListenerFailure(final Event event, final RegisteredListener registration, final Throwable throwable) {
        final String message = "Could not pass event " + event.getEventName() + " to " + registration.getPlugin().getPluginMeta().getDisplayName();
        this.server.getLogger().log(Level.SEVERE, message, throwable);
        if (!(event instanceof ServerExceptionEvent)) {
            this.callEvent(new ServerExceptionEvent(new ServerEventException(
                message,
                throwable,
                registration.getPlugin(),
                registration.getListener(),
                event
            )));
        }
    }
}
