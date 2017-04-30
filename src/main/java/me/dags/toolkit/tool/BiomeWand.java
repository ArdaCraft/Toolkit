package me.dags.toolkit.tool;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Join;
import me.dags.commandbus.annotation.Permission;
import me.dags.toolkit.Toolkit;
import me.dags.toolkit.utils.UserData;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.biome.BiomeType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class BiomeWand {

    @Permission("toolkit.wand.biome")
    @Command(alias = "biome", parent = "wand")
    public void biomeWandItem(@Caller Player player) {
        Optional<ItemStack> inHand = player.getItemInHand(HandTypes.MAIN_HAND);
        if (inHand.isPresent()) {
            ItemType type = inHand.get().getItem();
            Toolkit.getData(player).set("option.wand.biome.item", type);
            Utils.notify(player, "Set biome wand to: " + type.getName());
        } else {
            Toolkit.getData(player).remove("option.wand.biome.item");
            Utils.notify(player, "Unset biome wand");
        }
    }

    @Permission("toolkit.biomes")
    @Command(alias = "biomes")
    public void biomeList(@Caller CommandSource source) {
        List<Text> lines = Sponge.getRegistry().getAllOf(BiomeType.class).stream()
                .map(BiomeType::getName)
                .sorted()
                .distinct()
                .map(s -> Text.builder(s).onClick(TextActions.runCommand("/biome " + s)).build())
                .collect(Collectors.toList());

        PaginationList.builder()
                .contents(lines)
                .title(Text.builder("Biomes").color(TextColors.GREEN).build())
                .build()
                .sendTo(source);
    }

    @Permission("toolkit.wand.biome")
    @Command(alias = "biome")
    public void biomeType(@Caller Player player) {
        BlockSnapshot snapshot = Utils.targetBlock(player, 50);
        snapshot.getLocation().ifPresent(l -> biomeType(player, l.getBiome().getName()));
    }

    @Permission("toolkit.wand.biome")
    @Command(alias = "biome")
    public void biomeType(@Caller Player player, @Join("biome") String biome) {
        Sponge.getRegistry().getType(BiomeType.class, biome).ifPresent(biomeType -> {
            Toolkit.getData(player).set("option.wand.biome.type", biomeType);
            Utils.notify(player, "Set biome type to: " + biomeType.getName());
        });
    }

    @Listener
    public void onInteract(InteractItemEvent event, @Root Player player) {
        ItemType type = event.getItemStack().getType();
        UserData data = Toolkit.getData(player);
        if (data.presentAndEquals("option.wand.biome.item", type)) {
            Optional<BiomeType> biome = data.get("option.wand.biome.type");
            if (biome.isPresent()) {
                event.setCancelled(true);
                BlockSnapshot target = Utils.targetBlock(player, 50);
                target.getLocation().ifPresent(l -> l.getExtent().setBiome(l.getBiomePosition(), biome.get()));
            }
        }
    }
}
