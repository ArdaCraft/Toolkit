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
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Plugin(name = Toolkit.NAME, id = Toolkit.ID, version = Toolkit.VERSION)
public class Toolkit {

    public static final String ID = "toolkit";
    public static final String NAME = "Toolkit";
    public static final String VERSION = "1.1.1-SNAPSHOT";

    private static final Logger logger = LoggerFactory.getLogger("Toolkit");
    private static final Map<UUID, UserData> userData = new HashMap<>();

    public static UserData getData(Player player) {
        UserData data = userData.get(player.getUniqueId());
        if (data == null) {
            userData.put(player.getUniqueId(), data = new UserData());
        }
        return data;
    }

    private static UserData removeData(Player player) {
        return userData.remove(player.getUniqueId());
    }

    @Listener
    public void init(GameInitializationEvent event) {
        Map<String, Object> modules = new HashMap<>();
        modules.put("get", new ItemGet());
        modules.put("wand.biome", new BiomeWand());
        modules.put("wand.info", new InfoWand());
        modules.put("nophysics", new NoPhysics());
        modules.put("weatherlock", new WeatherLock());

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
}