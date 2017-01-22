package me.dags.toolkit.tool;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.toolkit.Toolkit;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;

/**
 * @author dags <dags@dags.me>
 */
public class NoPhysics {

    @Command(aliases = {"nophysics", "np"}, perm = @Permission("toolkit.nophysics"))
    public void togglePhysics(@Caller Player player) {
        boolean value = Toolkit.getData(player).transform("option.nophysics", b -> !b, () -> false);
        Utils.notify(player, "Set no-physics to: ", value);
    }

    @Listener
    public void onPlayerBlockUpdate(NotifyNeighborBlockEvent event, @Named(NamedCause.NOTIFIER) Player player) {
        if (Toolkit.getData(player).getOrElse("option.nophysics", false)) {
            event.setCancelled(true);
        }
    }
}
