package io.papermc.paper.plugin.manager;

import io.papermc.paper.event.debug.EventDebugHook;
import java.util.Map;
import java.util.Set;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class DelegatingEventManager extends PaperEventManager {

    protected final Server server;
    protected final PaperEventManager delegate;
    private volatile EventDebugEventManager debugEventManager;

    DelegatingEventManager(final Server server, final PaperEventManager delegate) {
        super(server);
        this.server = server;
        this.delegate = delegate;
    }

    @Override
    public void callEvent(final @NotNull Event event) {
        final EventDebugEventManager debugEventManager = this.debugEventManager;
        if (debugEventManager == null) {
            this.delegate.callEvent(event);
        } else {
            debugEventManager.callEvent(event);
        }
    }

    void updateDebugHook(final @Nullable EventDebugHook hook) {
        this.debugEventManager = hook == null ? null : new EventDebugEventManager(this.server, this.delegate, hook);
    }

    boolean hasDebugHook() {
        return this.debugEventManager != null;
    }

    @Override
    public void registerEvents(final @NotNull Listener listener, final @NotNull Plugin plugin) {
        this.delegate.registerEvents(listener, plugin);
    }

    @Override
    public void registerEvent(
        final @NotNull Class<? extends Event> event,
        final @NotNull Listener listener,
        final @NotNull EventPriority priority,
        final @NotNull EventExecutor executor,
        final @NotNull Plugin plugin
    ) {
        this.delegate.registerEvent(event, listener, priority, executor, plugin);
    }

    @Override
    public void registerEvent(
        final @NotNull Class<? extends Event> event,
        final @NotNull Listener listener,
        final @NotNull EventPriority priority,
        final @NotNull EventExecutor executor,
        final @NotNull Plugin plugin,
        final boolean ignoreCancelled
    ) {
        this.delegate.registerEvent(event, listener, priority, executor, plugin, ignoreCancelled);
    }

    @Override
    public @NotNull Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(
        final @NotNull Listener listener,
        final @NotNull Plugin plugin
    ) {
        return this.delegate.createRegisteredListeners(listener, plugin);
    }

    @Override
    public void clearEvents() {
        this.delegate.clearEvents();
    }
}
