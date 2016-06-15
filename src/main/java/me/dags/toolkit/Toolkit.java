package me.dags.toolkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;

import me.dags.commandbus.CommandBus;

@Plugin(name = "Toolkit", id = "toolkit", version = Toolkit.VERSION)
public class Toolkit {

    public static final String VERSION = "${version}-build${build.number}";
    private static final Map<UUID, UserData> userData = new HashMap<>();

    static UserData getData(Player player) {
        UserData data = userData.get(player.getUniqueId());
        if (data == null) {
            userData.put(player.getUniqueId(), data = new UserData());
        }
        return data;
    }

    static UserData removeData(Player player) {
        return userData.remove(player.getUniqueId());
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        CommandBus.newInstance().register(Commands.class).submit(this);
    }

    @Listener(order = Order.POST)
    public void onJoin(ClientConnectionEvent.Join event) {
        Toolkit.getData(event.getTargetEntity());
    }

    @Listener(order = Order.POST)
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        Toolkit.getData(event.getTargetEntity());
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @Named(NamedCause.NOTIFIER) Player player) {
        if (Toolkit.getData(player).getOrElse("option.nophysics", false)) {
            event.setCancelled(true);
        }
    }
}