package io.papermc.testplugin;

import io.papermc.paper.event.debug.EventDebugHooks;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class TestPlugin extends JavaPlugin implements Listener {

    private final StableValue<EventDebugHooks.Registration> cancellationDebugHook = StableValue.of();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.cancellationDebugHook.setOrThrow(EventDebugHooks.register(new CancellationDebugHook(this.getLogger())));

        // io.papermc.testplugin.brigtests.Registration.registerViaOnEnable(this);
    }

    @Override
    public void onDisable() {
        if (this.cancellationDebugHook.isSet()) {
            this.cancellationDebugHook.orElseThrow().close();
        }
    }
}
