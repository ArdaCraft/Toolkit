package me.dags.toolkit.tool;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import me.dags.toolkit.Toolkit;
import me.dags.toolkit.clipboard.Clipboard;
import me.dags.toolkit.clipboard.ClipboardOptions;
import me.dags.toolkit.clipboard.Selector;
import me.dags.toolkit.clipboard.block.Axis;
import me.dags.toolkit.clipboard.block.Facing;
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
public class CopyWand {

    public static final String WAND = "option.copy.wand";
    public static final String SELECTOR = "option.copy.selector";
    public static final String CLIPBOARD = "option.copy.clipboard";
    public static final String CLIPBOARD_OPTIONS = "option.copy.clipboard.options";

    @Permission("toolkit.copy")
    @Command(alias = "copy")
    public void copy(@Caller Player player) {
        Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);
        if (inHand.isPresent()) {
            FMT.info("Set copy wand to ").stress(inHand.get().getName()).tell(player);
            Toolkit.getData(player).remove(CopyWand.CLIPBOARD);
            Toolkit.getData(player).set(CopyWand.WAND, inHand.get());
            Toolkit.getData(player).set(CopyWand.SELECTOR, new Selector());
            Toolkit.getData(player).set(CopyWand.CLIPBOARD_OPTIONS, new ClipboardOptions());
        } else {
            FMT.info("Removed copy wand").tell(player);
            Toolkit.getData(player).remove(CopyWand.WAND);
            Toolkit.getData(player).remove(CopyWand.SELECTOR);
            Toolkit.getData(player).remove(CopyWand.CLIPBOARD);
            Toolkit.getData(player).remove(CopyWand.CLIPBOARD_OPTIONS);
        }
    }

    @Permission("toolkit.copy")
    @Command(alias = "range", parent = "copy")
    public void range(@Caller Player player, @One("range") int range) {
        Optional<Selector> selector = Toolkit.getData(player).get(CopyWand.SELECTOR);
        if (selector.isPresent()) {
            selector.get().setRange(Math.max(1, Math.min(range, 25)));
            Utils.notify(player, "Set range to ", selector.get().getRange());
        }
    }

    @Permission("toolkit.copy")
    @Command(alias = "reset", parent = "copy")
    public void reset(@Caller Player player) {
        FMT.info("Resetting copy wand").tell(player);
        Toolkit.getData(player).remove(CopyWand.SELECTOR);
        Toolkit.getData(player).remove(CopyWand.CLIPBOARD);
        Toolkit.getData(player).remove(CopyWand.CLIPBOARD_OPTIONS);
    }

    @Permission("toolkit.copy")
    @Command(alias = "rotate", parent = "copy")
    public void rotate(@Caller Player player) {
        Optional<ClipboardOptions> options = Toolkit.getData(player).get(CopyWand.CLIPBOARD_OPTIONS);
        if (options.isPresent()) {
            options.get().setAutoRotate(!options.get().autoRotate());
            Utils.notify(player,"Auto-rotate: ", options.get().autoRotate());
        }
    }

    @Permission("toolkit.copy")
    @Command(alias = "flip", parent = "copy")
    public void flip(@Caller Player player) {
        Optional<ClipboardOptions> options = Toolkit.getData(player).get(CopyWand.CLIPBOARD_OPTIONS);
        if (options.isPresent()) {
            Axis axis = Facing.facing(player).getAxis();
            if (axis == Axis.x) {
                options.get().setFlipX(!options.get().flipX());
                Utils.notify(player, "Flip X: ", options.get().flipX());
            } else if (axis == Axis.y) {
                options.get().setFlipY(!options.get().flipY());
                Utils.notify(player, "Flip Y: ", options.get().flipY());
            } else if (axis == Axis.z) {
                options.get().setFlipZ(!options.get().flipZ());
                Utils.notify(player, "Flip Z: ", options.get().flipZ());
            }
        }
    }

    @Permission("toolkit.copy")
    @Command(alias = "flip", parent = "copy random")
    public void randomFlip(@Caller Player player) {
        Optional<ClipboardOptions> options = Toolkit.getData(player).get(CopyWand.CLIPBOARD_OPTIONS);
        if (options.isPresent()) {
            Axis axis = Facing.facing(player).getAxis();
            if (axis == Axis.y) {
                options.get().setRandomFlipV(!options.get().randomFlipV());
                Utils.notify(player, "Randomly flip vertically: ", options.get().randomFlipV());
            } else {
                options.get().setRandomFlipH(!options.get().randomFlipH());
                Utils.notify(player, "Randomly flip horizontally: ", options.get().randomFlipH());
            }
        }
    }

    @Permission("toolkit.copy")
    @Command(alias = "rotate", parent = "copy random")
    public void randomRotate(@Caller Player player) {
        Optional<ClipboardOptions> options = Toolkit.getData(player).get(CopyWand.CLIPBOARD_OPTIONS);
        if (options.isPresent()) {
            options.get().setRandomRotate(!options.get().randomRotate());
            Utils.notify(player,"Set random rotation: ", options.get().randomRotate());
        }
    }

    @Listener
    public void interactPrimary(InteractItemEvent.Primary.MainHand event, @Root Player player) {
        Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);
        if (inHand.isPresent()) {
            Optional<ItemType> wand = Toolkit.getData(player).get(CopyWand.WAND);
            if (wand.isPresent() && wand.get() == inHand.get()) {
                event.setCancelled(true);

                Optional<Clipboard> clipBoard = Toolkit.getData(player).get(CopyWand.CLIPBOARD);
                if (clipBoard.isPresent()) {
                    if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                        Optional<Selector> selector = Toolkit.getData(player).get(CopyWand.SELECTOR);
                        selector.ifPresent(s -> s.reset(player));
                    } else {
                        Utils.notify(player, "Undoing paste....");
                        clipBoard.get().undo(player);
                    }
                }
            }
        }
    }

    @Listener
    public void interactSecondary(InteractItemEvent.Secondary.MainHand event, @Root Player player) {
        Optional<ItemType> inHand = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::getItem);

        if (inHand.isPresent()) {
            Optional<ItemType> wand = Toolkit.getData(player).get(CopyWand.WAND);

            if (wand.isPresent() && wand.get() == inHand.get()) {
                event.setCancelled(true);

                Selector selector = Toolkit.getData(player).get(CopyWand.SELECTOR, Selector::new);
                Optional<Clipboard> clipBoard = Toolkit.getData(player).get(CopyWand.CLIPBOARD);
                Vector3i target = Utils.targetPosition(player, selector.getRange());

                if (clipBoard.isPresent()) {
                    clipBoard.get().paste(player, target, Toolkit.getPlayerCause(player));
                } else {
                    selector.pos(player, target);
                }
            }
        }
    }
}
