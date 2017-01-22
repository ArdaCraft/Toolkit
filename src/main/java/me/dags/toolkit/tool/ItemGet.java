package me.dags.toolkit.tool;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * @author dags <dags@dags.me>
 */
public class ItemGet {

    @Command(aliases = "get", perm = @Permission("toolkit.get.block"))
    public void getBlock(@Caller Player player) {
        BlockSnapshot target = Utils.targetBlock(player, 50);
        if (target.getState().getType().getItem().isPresent()) {
            ItemStack itemStack = ItemStack.builder().fromBlockSnapshot(target).quantity(1).build();
            player.getInventory().offer(itemStack);
            Utils.notify(player, target.getState().getName());
        }
    }

    @Command(aliases = "head", parent = "get", perm = @Permission("toolkit.get.head"))
    public void getHead(@Caller Player player, @One("player") User user) {
        ItemStack stack = ItemStack.of(ItemTypes.SKULL, 1);
        stack.offer(Keys.SKULL_TYPE, SkullTypes.PLAYER);
        stack.offer(Keys.SKIN_UNIQUE_ID, user.getUniqueId());
        player.getInventory().offer(stack);
    }
}
