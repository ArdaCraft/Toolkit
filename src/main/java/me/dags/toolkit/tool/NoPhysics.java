package me.dags.toolkit.tool;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.toolkit.Toolkit;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class NoPhysics {

    private final Override override = new Override();

    @Command("np")
    @Permission("toolkit.nophysics.user")
    @Description("Toggle physics on your block interactions")
    public void userPhysics(@Src Player player) {
        boolean value = Toolkit.getData(player).transform("option.nophysics", b -> !b, () -> true);
        Utils.notify(player, "Set no-physics to: ", value);
    }

    @Command("np global")
    @Permission("toolkit.nophysics.global")
    @Description("Toggle physics on non-player interactions")
    public void worldPhysics(@Src Player player) {
        if (override.isActive()) {
            override.disable();
            Fmt.stress("Worldwide physics disabled").tell(Sponge.getServer().getBroadcastChannel());
        } else {
            override.enable(player.getUniqueId());
            Fmt.warn("Worldwide physics enabled").tell(Sponge.getServer().getBroadcastChannel());
        }
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        if (override.isOwner(event.getTargetEntity().getUniqueId())) {
            override.disable();
            Fmt.stress("Worldwide physics disabled").tell(Sponge.getServer().getBroadcastChannel());
        }
    }

    @Listener
    public void onPlayerBlockUpdate(NotifyNeighborBlockEvent event) {
        Optional<Player> notifier = event.getContext().get(EventContextKeys.NOTIFIER).flatMap(User::getPlayer);

        // event not caused by a player interaction
        if (!notifier.isPresent()) {
            // override is not active. default behaviour is to prevent updates
            if (!override.isActive()) {
                event.setCancelled(true);
            }
            return;
        }

        // player has enabled no-physics
        if (Toolkit.getData(notifier.get()).getOrElse("option.nophysics", true)) {
            event.setCancelled(true);
        }
    }

    private static class Override {

        private UUID owner = null;

        private void enable(UUID uuid) {
            owner = uuid;
        }

        private void disable() {
            owner = null;
        }

        private boolean isActive() {
            return owner != null;
        }

        private boolean isOwner(UUID uuid) {
            return owner != null && owner == uuid;
        }
    }
}
