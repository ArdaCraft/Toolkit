package me.dags.toolkit.tool;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.toolkit.Toolkit;
import me.dags.toolkit.clipboard.Clipboard;
import me.dags.toolkit.clipboard.Transform;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class SelectWand {

    private static int maxSize = 50000;

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
            Toolkit.getData(player).remove("option.wand.select.volume");
            Toolkit.getData(player).remove("option.wand.select.selector");
            Toolkit.getData(player).remove("option.wand.select.transform");
        }
    }

    @Permission("toolkit.select")
    @Command(alias = "range", parent = "wand select")
    public void range(@Caller Player player, @One("range") int range) {
        Optional<Selector> selector = Toolkit.getData(player).get("option.wand.select.selector");
        if (selector.isPresent()) {
            selector.get().range = Math.max(1, Math.min(range, 25));
            Utils.notify(player, "Set range to ", selector.get().range);
        }
    }

    @Permission("toolkit.select")
    @Command(alias = "reset", parent = "wand select")
    public void reset(@Caller Player player) {
        FMT.info("Resetting selection wand").tell(player);
        Toolkit.getData(player).remove("option.wand.select.volume");
        Toolkit.getData(player).remove("option.wand.select.selector");
        Toolkit.getData(player).remove("option.wand.select.transform");
    }

    @Permission("toolkit.select")
    @Command(alias = "rotate", parent = "wand select")
    public void rotate(@Caller Player player, @One("rotate") boolean rotate) {
        Optional<Transform> transform = Toolkit.getData(player).get("option.wand.select.transform");
        if (transform.isPresent()) {
            Utils.notify(player,"Set rotate: ", rotate);
            transform.get().rotate(rotate);
        }
    }

    @Permission("toolkit.select")
    @Command(alias = "flip", parent = "wand select")
    public void flip(@Caller Player player, @One("flip") boolean flip) {
        Vector3d rotation = player.getRotation();
        if (rotation.getX() > 45 || rotation.getX() < -45) {
            flipY(player, flip);
        } else {
            Direction direction = Utils.direction(Utils.directionVector(rotation));

            if (direction == Direction.EAST || direction == Direction.WEST) {
                flipZ(player, flip);
            } else {
                flipX(player, flip);
            }
        }
    }

    @Permission("toolkit.select")
    @Command(alias = "x", parent = "wand select")
    public void flipX(@Caller Player player, @One("flip x") boolean flipX) {
        Optional<Transform> transform = Toolkit.getData(player).get("option.wand.select.transform");
        if (transform.isPresent()) {
            Utils.notify(player,"Set flip X: ", flipX);
            transform.get().flipX(flipX);
        }
    }

    @Permission("toolkit.select")
    @Command(alias = "y", parent = "wand select")
    public void flipY(@Caller Player player, @One("flip y") boolean flipY) {
        Optional<Transform> transform = Toolkit.getData(player).get("option.wand.select.transform");
        if (transform.isPresent()) {
            Utils.notify(player,"Set flip Y: ", flipY);
            transform.get().flipY(flipY);
        }
    }

    @Permission("toolkit.select")
    @Command(alias = "z", parent = "wand select")
    public void flipZ(@Caller Player player, @One("flip z") boolean flipZ) {
        Optional<Transform> transform = Toolkit.getData(player).get("option.wand.select.transform");
        if (transform.isPresent()) {
            Utils.notify(player,"Set flip Z: ", flipZ);
            transform.get().flipZ(flipZ);
        }
    }

    @Permission("toolkit.select")
    @Command(alias = "rotate", parent = "wand select random")
    public void randomRotate(@Caller Player player, @One("random") boolean random) {
        Optional<Transform> transform = Toolkit.getData(player).get("option.wand.select.transform");
        if (transform.isPresent()) {
            Utils.notify(player,"Set random rotation: ", random);
            transform.get().randomRotate(random);
        }
    }

    @Permission("toolkit.select")
    @Command(alias = "vertical", parent = "wand select random")
    public void randomVertical(@Caller Player player, @One("random") boolean random) {
        Optional<Transform> transform = Toolkit.getData(player).get("option.wand.select.transform");
        if (transform.isPresent()) {
            Utils.notify(player,"Set random vertical flip: ", random);
            transform.get().randomVertical(random);
        }
    }

    @Permission("toolkit.select")
    @Command(alias = "horizontal", parent = "wand select random")
    public void randomHorizontal(@Caller Player player, @One("random") boolean random) {
        Optional<Transform> transform = Toolkit.getData(player).get("option.wand.select.transform");
        if (transform.isPresent()) {
            Utils.notify(player,"Set random horizontal flip: ", random);
            transform.get().randomHorizontal(random);
        }
    }

    @Listener
    public void interact(InteractItemEvent.Secondary.MainHand event, @Root Player player) {
        Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);

        if (inHand.isPresent()) {
            Optional<ItemType> wand = Toolkit.getData(player).get("option.wand.select.item");
            if (wand.isPresent() && wand.get() == inHand.get()) {
                event.setCancelled(true);

                Selector selector = Toolkit.getData(player).get("option.wand.select.selector", Selector::new);
                Vector3i target = Utils.targetPosition(player, selector.range);

                Optional<Clipboard> clipBoard = Toolkit.getData(player).get("option.wand.select.volume");
                if (clipBoard.isPresent()) {
                    Utils.notify(player, "Pasting...");
                    clipBoard.get().apply(player, target, Toolkit.getCause(player));
                } else {
                    selector.pos(player, target);
                }
            }
        }
    }

    @Listener
    public void interact(InteractItemEvent.Primary.MainHand event, @Root Player player) {
        Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);
        if (inHand.isPresent()) {
            Optional<ItemType> wand = Toolkit.getData(player).get("option.wand.select.item");
            if (wand.isPresent() && wand.get() == inHand.get()) {
                event.setCancelled(true);

                Optional<Clipboard> clipBoard = Toolkit.getData(player).get("option.wand.select.volume");
                if (clipBoard.isPresent()) {
                    if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                        Utils.notify(player, "Clearing clipboard & selection points");
                        Toolkit.getData(player).remove("option.wand.select.volume");
                        Toolkit.getData(player).remove("option.wand.select.selector");
                    } else {
                        Utils.notify(player, "Undoing paste....");
                        clipBoard.get().undo();
                    }
                }
            }
        }
    }

    private static class Selector {

        private int range = 25;
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
                if (size(min, max) <= maxSize) {
                    Clipboard clipboard = Clipboard.of(player, min, max, pos);
                    Toolkit.getData(player).set("option.wand.select.volume", clipboard);
                    Utils.notify(player, "Copied selection");
                } else {
                    Utils.error(player, "Selection size is too large!");
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
