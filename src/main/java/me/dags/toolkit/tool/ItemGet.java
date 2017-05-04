package me.dags.toolkit.tool;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * @author dags <dags@dags.me>
 */
public class ItemGet {

    @Permission("toolkit.get.block")
    @Command(alias = "get")
    public void getBlock(@Caller Player player) {
        BlockSnapshot target = Utils.targetBlock(player, 50);
        if (target.getState().getType().getItem().isPresent()) {
            ItemStack itemStack = ItemStack.builder().fromBlockSnapshot(target).quantity(1).build();
            player.getInventory().offer(itemStack);
            Utils.notify(player, target.getState().getName());
        }
    }

    @Permission("toolkit.get.head")
    @Command(alias = "head", parent = "get")
    public void getHead(@Caller Player player, @One("player") User user) {
        String command = String.format("give %s minecraft:skull 1 3 {SkullOwner:%s}", player.getName(), user.getName());
        Sponge.getCommandManager().process(player, command);
    }

    @Permission("toolkit.get.head")
    @Command(alias = "head", parent = "get")
    public void getHead(@Caller Player player, @One("player") String user) {
        String command = String.format("give %s minecraft:skull 1 3 {SkullOwner:%s}", player.getName(), user);
        Sponge.getCommandManager().process(player, command);
    }
}
