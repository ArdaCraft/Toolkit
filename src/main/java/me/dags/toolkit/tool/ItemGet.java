package me.dags.toolkit.tool;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * @author dags <dags@dags.me>
 */
public class ItemGet {

    @Permission("toolkit.get.block")
    @Command("get")
    public void getBlock(@Src Player player) {
        BlockSnapshot target = Utils.targetBlock(player, 50);
        if (target.getState().getType().getItem().isPresent()) {
            ItemStack itemStack = ItemStack.builder().fromBlockSnapshot(target).quantity(1).build();
            player.getInventory().offer(itemStack);
            Utils.notify(player, target.getState().getName());
        }
    }

    @Permission("toolkit.get.head")
    @Command("get head <player>")
    public void getHead(@Src Player player, User user) {
        getHead(player, user.getName());
    }

    @Permission("toolkit.get.head")
    @Command("get head <name>")
    public void getHead(@Src Player player, String name) {
        DataContainer container = new MemoryDataContainer()
                .set(DataQuery.of("ItemType"), ItemTypes.SKULL)
                .set(DataQuery.of("Count"), 1)
                .set(DataQuery.of("UnsafeDamage"), 3)
                .set(DataQuery.of("UnsafeData", "SkullOwner"), name);

        Utils.notify(player, "Got ", name, "'s head!");
        player.getInventory().offer(ItemStack.builder().fromContainer(container).build());
    }
}
