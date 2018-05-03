package me.dags.toolkit.tool;

import java.util.Optional;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.commandbus.fmt.Formatter;
import me.dags.toolkit.Toolkit;
import me.dags.toolkit.utils.UserData;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.user.UserStorageService;

/**
 * @author dags <dags@dags.me>
 */
public class InfoWand {

    @Permission("toolkit.info")
    @Command("wand info")
    public void getBlock(@Src Player player) {
        Optional<ItemStack> inHand = player.getItemInHand(HandTypes.MAIN_HAND);
        if (inHand.isPresent()) {
            ItemType type = inHand.get().getItem();
            Toolkit.getData(player).set("option.wand.info.item", type);
            Utils.notify(player, "Set info wand to: " + type.getName());
        } else {
            Toolkit.getData(player).remove("option.wand.info.item");
            Utils.error(player, "Unset info wand");
        }
    }

    @Listener
    public void onInteract(InteractItemEvent.Secondary event, @Root Player player) {
        ItemType type = event.getItemStack().getType();
        UserData data = Toolkit.getData(player);
        if (data.presentAndEquals("option.wand.info.item", type)) {
            BlockSnapshot target = Utils.targetBlock(player, 10);

            Formatter info = Fmt.info("Position: ").stress(target.getPosition());

            UserStorageService users = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
            target.getCreator()
                    .flatMap(users::get)
                    .ifPresent(user -> info.info(", Creator: ").stress(user.getName()));

            target.getNotifier()
                    .flatMap(users::get)
                    .ifPresent(user -> info.info(", Notifier: ").stress(user.getName()));

            info.tell(player);
        }
    }
}
