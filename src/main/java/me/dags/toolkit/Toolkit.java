package me.dags.toolkit;

import me.dags.commandbus.CommandBus;
import me.dags.toolkit.tool.BiomeWand;
import me.dags.toolkit.tool.ItemGet;
import me.dags.toolkit.tool.NoPhysics;
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

@Plugin(name = Properties.NAME, id = Properties.ID, version = Properties.VERSION)
public class Toolkit {

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
        modules.put("nophysics", new NoPhysics());

        // TODO - check against config to see if tool is enabled before registering it
        CommandBus commandBus = CommandBus.newInstance(logger);
        modules.values().forEach(commandBus::register);
        modules.values().forEach(m -> Sponge.getEventManager().registerListeners(this, m));
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