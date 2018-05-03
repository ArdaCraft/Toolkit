package me.dags.toolkit.utils;

import java.util.Optional;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class Utils {

    public static void notify(Player player, Object... message) {
        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(message));
    }

    public static void error(Player player, Object... message) {
        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(message).toBuilder().color(TextColors.RED).build());
    }

    public static BlockSnapshot targetBlock(Player player, int limit) {
        Optional<BlockRayHit<World>> hit = BlockRay.from(player)
                .stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
                .distanceLimit(limit)
                .end();

        return hit.map(blockHit -> blockHit.getLocation().createSnapshot()).orElse(BlockSnapshot.NONE);
    }
}
