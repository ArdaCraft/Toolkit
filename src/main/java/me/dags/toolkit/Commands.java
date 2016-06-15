package me.dags.toolkit;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;

public class Commands {

    @Command(aliases = {"nophysics", "np"}, parent = "toolkit", perm = "toolkit.nophysics.use")
    public void togglePhysics(@Caller Player player) {
        boolean value = Toolkit.getData(player).transform("option.nophysics", b -> !b, () -> false);
        player.sendMessage(Text.of("Set nophysics to: " + value));
    }
}
