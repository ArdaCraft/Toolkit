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
        ItemStack stack = ItemStack.builder()
                .itemType(ItemTypes.SKULL)
                .add(Keys.SKULL_TYPE, SkullTypes.PLAYER)
                .add(Keys.SKIN_UNIQUE_ID, user.getUniqueId())
                .build();

        player.getInventory().offer(stack);
    }
}
