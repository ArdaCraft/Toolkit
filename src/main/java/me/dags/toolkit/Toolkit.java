package me.dags.toolkit;

import me.dags.commandbus.CommandBus;
import me.dags.toolkit.tool.*;
import me.dags.toolkit.utils.UserData;
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
    public static final String VERSION = "1.3.0-SNAPSHOT";
    private static final Map<UUID, UserData> userData = new HashMap<>();

    private static Toolkit instance;

    @Listener
    public void init(GameInitializationEvent event) {
        Map<String, Object> modules = new HashMap<>();
        modules.put("get", new ItemGet());
        modules.put("wand.biome", new BiomeWand());
        modules.put("wand.info", new InfoWand());
        modules.put("nophysics", new NoPhysics());
        modules.put("weatherlock", new WeatherLock());

        CommandBus commandBus = CommandBus.create(this);
        // TODO - check against config to see if tool is enabled before registering it
        modules.values().forEach(commandBus::register);
        modules.values().forEach(m -> Sponge.getEventManager().registerListeners(this, m));

        commandBus.register(this).submit();
    }

    @Listener(order = Order.POST)
    public void join(ClientConnectionEvent.Join event) {
        Toolkit.getData(event.getTargetEntity());
    }

    @Listener(order = Order.POST)
    public void quit(ClientConnectionEvent.Disconnect event) {
        Toolkit.removeData(event.getTargetEntity());
    }

    public static UserData getData(UUID id) {
        return userData.computeIfAbsent(id, k -> new UserData());
    }

    public static UserData getData(Player player) {
        return getData(player.getUniqueId());
    }

    public static Toolkit getInstance() {
        return instance;
    }

    private static UserData removeData(Player player) {
        return userData.remove(player.getUniqueId());
    }
}