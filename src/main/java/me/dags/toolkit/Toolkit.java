package me.dags.toolkit;

import me.dags.commandbus.CommandBus;
import me.dags.toolkit.tool.*;
import me.dags.toolkit.utils.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Plugin(name = Toolkit.NAME, id = Toolkit.ID, version = Toolkit.VERSION)
public class Toolkit {

    public static final String ID = "toolkit";
    public static final String NAME = "Toolkit";
    public static final String VERSION = "1.2.2-SNAPSHOT";

    private static final Logger logger = LoggerFactory.getLogger("Toolkit");
    private static final Map<UUID, UserData> userData = new HashMap<>();
    private static Cause globalCause;
    private static PluginContainer container;
    private static SpongeExecutorService syncExecutor;
    private static SpongeExecutorService asyncExecutor;

    @Inject
    public Toolkit(PluginContainer container) {
        Toolkit.container = container;
        Toolkit.globalCause = Cause.source(container).build();
        Toolkit.syncExecutor = Sponge.getScheduler().createSyncExecutor(this);
        Toolkit.asyncExecutor = Sponge.getScheduler().createAsyncExecutor(this);
    }

    @Listener
    public void init(GameInitializationEvent event) {
        Map<String, Object> modules = new HashMap<>();
        modules.put("get", new ItemGet());
        modules.put("wand.biome", new BiomeWand());
        modules.put("wand.info", new InfoWand());
        modules.put("wand.copy", new CopyWand());
        modules.put("nophysics", new NoPhysics());
        modules.put("weatherlock", new WeatherLock());
        modules.put("commandbook", new CommandBook());

        // TODO - check against config to see if tool is enabled before registering it
        CommandBus commandBus = CommandBus.builder().logger(logger).build();
        modules.values().forEach(commandBus::register);
        modules.values().forEach(m -> Sponge.getEventManager().registerListeners(this, m));

        // test
        commandBus.register(new LightFixer());

        commandBus.submit(this);
    }

    @Listener(order = Order.POST)
    public void join(ClientConnectionEvent.Join event) {
        Toolkit.getData(event.getTargetEntity());
    }

    @Listener(order = Order.POST)
    public void quit(ClientConnectionEvent.Disconnect event) {
        Toolkit.removeData(event.getTargetEntity());
    }

    public static void submitAsyncTask(Runnable runnable) {
        asyncExecutor.submit(runnable);
    }

    public static void submitSyncTask(Runnable runnable) {
        syncExecutor.submit(runnable);
    }

    public static Cause getGlobalCause() {
        return globalCause;
    }

    public static Cause getPlayerCause(Player player) {
        return Cause.source(container)
                .notifier(player)
                .owner(player)
                .build();
    }

    public static UserData getData(Player player) {
        return userData.computeIfAbsent(player.getUniqueId(), k -> new UserData());
    }

    private static UserData removeData(Player player) {
        return userData.remove(player.getUniqueId());
    }
}