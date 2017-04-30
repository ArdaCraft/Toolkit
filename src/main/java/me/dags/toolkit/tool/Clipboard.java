package me.dags.toolkit.tool;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ArchetypeVolume;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Clipboard {

    private final Direction direction;
    private final ArchetypeVolume blocks;
    private final Map<EntityArchetype, Vector3d> entities;

    private List<BlockSnapshot> history = new LinkedList<>();

    private Clipboard(ArchetypeVolume volume, Map<EntityArchetype, Vector3d> entities, Direction direction) {
        this.blocks = volume;
        this.entities = entities;
        this.direction = direction;
    }

    public void apply(Player player, Vector3i position, Cause cause) {
        Direction pasteDir = Utils.direction(Utils.directionVector(player.getRotation()));

        int angle = Utils.getAngle(direction, pasteDir);
        double rads = Math.toRadians(angle);
        World world = player.getWorld();

        history = new LinkedList<>();

        blocks.getBlockWorker(cause).iterate((v, x, y, z) -> {
            BlockState state = v.getBlock(x, y, z);

            if (state.getType() == BlockTypes.AIR) {
                return;
            }

            int xPos = position.getX() + rotateY(x, z, rads, -1);
            int yPos = position.getY() + y;
            int zPos = position.getZ() + rotateY(z, x, rads, 1);

            state = rotateFacing(state, angle);
            state = rotateAxis(state, angle);

            history.add(world.createSnapshot(xPos, yPos, zPos));
            world.setBlock(xPos, yPos, zPos, state, BlockChangeFlag.NONE, cause);
        });
    }

    public void undo() {
        for (BlockSnapshot snapshot : history) {
            snapshot.restore(true, BlockChangeFlag.NONE);
        }
    }

    private static int rotateY(int a, int b, double rads, int sign) {
        return (int) Math.round(a * Math.cos(rads) + (sign * b) * Math.sin(rads));
    }

    private static BlockState rotateFacing(BlockState state, int angle) {
        Optional<BlockTrait<?>> facing = state.getTrait("facing");
        if (facing.isPresent()) {
            Optional<?> val = state.getTraitValue(facing.get());
            if (val.isPresent()) {
                Object rotated = Utils.rotateFacing(facing.get(), val.get(), angle);
                if (rotated != null) {
                    return state.withTrait(facing.get(), rotated).orElse(state);
                }
            }
        }
        return state;
    }

    private static BlockState rotateAxis(BlockState state, int angle) {
        Optional<BlockTrait<?>> axis = state.getTrait("axis");
        if (axis.isPresent()) {
            Optional<?> val = state.getTraitValue(axis.get());
            if (val.isPresent()) {
                Object rotated = Utils.rotateAxis(axis.get(), val.get(), angle);
                if (rotated != null) {
                    return state.withTrait(axis.get(), rotated).orElse(state);
                }
            }
        }
        return state;
    }

    public static Clipboard of(Player player, Vector3i min, Vector3i max, Vector3i origin) {
        Vector3d entityOrigin = origin.toDouble();
        ImmutableMap.Builder<EntityArchetype, Vector3d> builder = ImmutableMap.builder();
        player.getWorld().getExtentView(min, max).getEntities().forEach(entity -> {
            Vector3d offset = entity.getLocation().getPosition().sub(entityOrigin);
            builder.put(entity.createArchetype(), offset);
        });

        Direction direction = Utils.direction(Utils.directionVector(player.getRotation()));
        ArchetypeVolume blocks = player.getWorld().createArchetypeVolume(min, max, origin);

        return new Clipboard(blocks, builder.build(), direction);
    }
}
