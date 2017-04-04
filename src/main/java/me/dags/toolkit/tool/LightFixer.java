package me.dags.toolkit.tool;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.format.FMT;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class LightFixer {

    @Permission("toolkit.fixerer")
    @Command(alias = "fixlight")
    public void fixLight(@Caller Player player, int radius) {
        Sponge.getPluginManager().getPlugin("toolkit").ifPresent(container -> {
            int rads = Math.min(250, Math.max(1, radius));
            Vector3i pos = player.getLocation().getBlockPosition();
            Vector3i min = new Vector3i(pos.getX() - rads, 255, pos.getZ() - rads);
            Vector3i max = new Vector3i(pos.getX() + rads, 255, pos.getZ() + rads);
            Cause place = Cause.source(container).build();
            Fixer fixer = new Fixer(player.getWorld(), min, max, pos.add(0, -10, 0).getY(), place);
            container.getInstance().ifPresent(Task.builder().execute(fixer).intervalTicks(1).delayTicks(1)::submit);
            FMT.info("Running fixerererer. Radius=").stress(rads).tell(player);
        });
    }

    private static class Fixer implements Consumer<Task> {

        private static final int PLACE = 0;
        private static final int BREAK = 1;

        private final World world;
        private final Vector3i min;
        private final Vector3i max;
        private final Cause place;

        private int x;
        private int z;
        private int y = 0;
        private int stage = PLACE;
        private BlockState previous = BlockTypes.AIR.getDefaultState();

        private Fixer(World world, Vector3i min, Vector3i max, int y, Cause place) {
            this.world = world;
            this.min = min;
            this.max = max;
            this.place = place;
            this.x = min.getX();
            this.y = y;
            this.z = min.getZ() - 1;
        }

        @Override
        public void accept(Task task) {
            if (stage == PLACE) {
                z++;
                previous = world.getBlock(x, y, z);
                world.setBlockType(x, y, z, BlockTypes.BEDROCK, place);
                stage = BREAK;
            } else {
                world.setBlock(x, y, z, previous, place);
                stage = PLACE;
                if (x == max.getX() && z == max.getZ()) {
                    task.cancel();
                } else if (z == max.getZ()) {
                    z = min.getZ() - 1;
                    x++;
                }
            }
        }
    }
}
