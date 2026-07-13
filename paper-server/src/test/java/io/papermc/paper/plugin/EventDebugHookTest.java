package io.papermc.paper.plugin;

import io.papermc.paper.event.debug.EventDebugHook;
import io.papermc.paper.event.debug.EventDebugHooks;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.support.environment.Normal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Normal
class EventDebugHookTest {

    private static final PluginManager PLUGIN_MANAGER = Bukkit.getPluginManager();

    @AfterEach
    void tearDown() {
        PLUGIN_MANAGER.clearPlugins();
    }

    @Test
    void observesEventAndListenerDispatch() {
        final List<String> calls = new ArrayList<>();
        final PaperTestPlugin plugin = new PaperTestPlugin("event-debug-hook-test");
        final Listener listener = new Listener() {
        };
        PLUGIN_MANAGER.registerEvent(TestEvent.class, listener, EventPriority.NORMAL, (ignored, event) -> calls.add("executor"), plugin);

        final EventDebugHook hook = new EventDebugHook() {
            @Override
            public void onEventStart(final Event event) {
                calls.add("event-start:" + event.getEventName());
            }

            @Override
            public void onListenerStart(final Event event, final RegisteredListener registeredListener) {
                calls.add("listener-start:" + registeredListener.getPlugin().getName());
            }

            @Override
            public void onListenerEnd(final Event event, final RegisteredListener registeredListener, final Throwable throwable) {
                calls.add("listener-end:" + throwable);
            }

            @Override
            public void onEventEnd(final Event event) {
                calls.add("event-end:" + event.getEventName());
            }
        };

        try (final EventDebugHooks.Registration ignored = EventDebugHooks.register(hook)) {
            PLUGIN_MANAGER.callEvent(new TestEvent(false));
        }

        assertEquals(List.of(
            "event-start:TestEvent",
            "listener-start:event-debug-hook-test",
            "executor",
            "listener-end:null",
            "event-end:TestEvent"
        ), calls);
    }

    @Test
    void closedRegistrationStopsObservingEvents() {
        final List<Event> events = new ArrayList<>();
        final EventDebugHooks.Registration registration = EventDebugHooks.register(new EventDebugHook() {
            @Override
            public void onEventStart(final Event event) {
                events.add(event);
            }
        });
        registration.close();
        registration.close();

        PLUGIN_MANAGER.callEvent(new TestEvent(false));

        assertEquals(List.of(), events);
    }
}
