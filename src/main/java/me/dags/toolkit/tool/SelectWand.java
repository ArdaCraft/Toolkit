package me.dags.toolkit.tool;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.toolkit.Toolkit;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class SelectWand {

    @Permission("toolkit.select")
    @Command(alias = "select", parent = "wand")
    public void select(@Caller Player player) {
        Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);
        if (inHand.isPresent()) {
            FMT.info("Set select wand to ").stress(inHand.get().getName()).tell(player);
            Toolkit.getData(player).set("option.wand.select.item", inHand.get());
        } else {
            FMT.info("Removed selection wand").tell(player);
            Toolkit.getData(player).remove("option.wand.select.item");
            Toolkit.getData(player).remove("option.wand.select.selector");
            Toolkit.getData(player).remove("option.wand.select.volume");
        }
    }

    @Listener
    public void interact(InteractItemEvent.Secondary.MainHand event, @Root Player player) {
        Vector3i target = Utils.targetPosition(player, 25);
        Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);

        if (target != Vector3i.ZERO && inHand.isPresent()) {
            Optional<ItemType> wand = Toolkit.getData(player).get("option.wand.select.item");
            if (wand.isPresent() && wand.get().equals(inHand.get())) {
                event.setCancelled(true);
                Optional<Clipboard> clipBoard = Toolkit.getData(player).get("option.wand.select.volume");
                if (clipBoard.isPresent()) {
                    Utils.notify(player, "Pasting...");
                    clipBoard.get().apply(player, target, Toolkit.getCause(player));
                } else {
                    Toolkit.getData(player).get("option.wand.select.selector", Selector::new).pos(player, target);
                }
            }
        }
    }

    @Listener
    public void interact(InteractItemEvent.Primary.MainHand event, @Root Player player) {
        Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);
        if (inHand.isPresent()) {
            event.setCancelled(true);
            Optional<Clipboard> clipBoard = Toolkit.getData(player).get("option.wand.select.volume");
            if (clipBoard.isPresent()) {
                if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                    Utils.notify(player, "Clearing clipboard");
                    Toolkit.getData(player).remove("option.wand.select.volume");
                } else {
                    Utils.notify(player, "Undoing paste....");
                    clipBoard.get().undo();
                }
            } else {
                Utils.notify(player, "Resetting selection points");
                Toolkit.getData(player).remove("option.wand.select.selector");
            }
        }
    }

    private static class Selector {

        private Vector3i pos1 = Vector3i.ZERO;
        private Vector3i pos2 = Vector3i.ZERO;

        private void pos(Player player, Vector3i pos) {
            if (pos1 == Vector3i.ZERO) {
                pos1 = pos;
                Utils.notify(player, "Set pos1 " + pos);
            } else if (pos2 == Vector3i.ZERO) {
                pos2 = pos;
                Utils.notify(player, "Set pos2 " + pos);
            } else if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                Vector3i min = pos1.min(pos2);
                Vector3i max = pos1.max(pos2);
                if (size(min, max) <= 100000) {
                    Clipboard clipboard = Clipboard.of(player, min, max, pos);
                    Toolkit.getData(player).set("option.wand.select.volume", clipboard);
                    Utils.notify(player, "Copied selection");
                } else {
                    Utils.error(player, "Selection size too large!");
                }
            } else {
                pos1 = Vector3i.ZERO;
                pos2 = Vector3i.ZERO;
                Toolkit.getData(player).remove("option.wand.select.volume");
                Utils.notify(player, "Cleared selection points");
            }
        }

        private int size(Vector3i min, Vector3i max) {
            int lx = max.getX() - min.getX();
            int ly = max.getY() - min.getY();
            int lz = max.getZ() - min.getZ();
            return lx * ly * lz;
        }
    }
}
