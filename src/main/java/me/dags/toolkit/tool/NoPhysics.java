package me.dags.toolkit.tool;

import java.util.Optional;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.toolkit.Toolkit;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;

/**
 * @author dags <dags@dags.me>
 */
public class NoPhysics {

    @Permission("toolkit.nophysics")
    @Command("np")
    public void togglePhysics(@Src Player player) {
        boolean value = Toolkit.getData(player).transform("option.nophysics", b -> !b, () -> false);
        Utils.notify(player, "Set no-physics to: ", value);
    }

    @Listener
    public void onPlayerBlockUpdate(NotifyNeighborBlockEvent event) {
        Optional<Player> notifier = event.getContext().get(EventContextKeys.NOTIFIER).flatMap(User::getPlayer);
        if (!notifier.isPresent()) {
            event.setCancelled(true);
            return;
        }

        if (Toolkit.getData(notifier.get()).getOrElse("option.nophysics", true)) {
            event.setCancelled(true);
        }
    }
}
