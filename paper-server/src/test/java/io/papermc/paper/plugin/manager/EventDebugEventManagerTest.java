package io.papermc.paper.plugin.manager;

import io.papermc.paper.event.debug.EventDebugHook;
import io.papermc.paper.event.debug.EventDebugHooks;
import org.bukkit.Bukkit;
import org.bukkit.support.environment.Normal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Normal
class EventDebugEventManagerTest {

    @Test
    void installsAndRemovesDelegatingEventManager() {
        final PaperPluginManagerImpl pluginManager = (PaperPluginManagerImpl) Bukkit.getPluginManager();
        final DelegatingEventManager eventManager = pluginManager.paperEventManager;
        assertFalse(eventManager.hasDebugHook());

        try (final EventDebugHooks.Registration ignored = EventDebugHooks.register(new EventDebugHook() {
        })) {
            assertSame(eventManager, pluginManager.paperEventManager);
            assertTrue(eventManager.hasDebugHook());
        }

        assertSame(eventManager, pluginManager.paperEventManager);
        assertFalse(eventManager.hasDebugHook());
    }

    @Test
    void remainsInstalledUntilLastHookIsRemoved() {
        final PaperPluginManagerImpl pluginManager = (PaperPluginManagerImpl) Bukkit.getPluginManager();
        final DelegatingEventManager eventManager = pluginManager.paperEventManager;
        final EventDebugHooks.Registration first = EventDebugHooks.register(new EventDebugHook() {
        });
        final EventDebugHooks.Registration second = EventDebugHooks.register(new EventDebugHook() {
        });
        try {
            first.close();
            assertSame(eventManager, pluginManager.paperEventManager);
            assertTrue(eventManager.hasDebugHook());
        } finally {
            first.close();
            second.close();
        }

        assertSame(eventManager, pluginManager.paperEventManager);
        assertFalse(eventManager.hasDebugHook());
    }
}
