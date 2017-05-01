package me.dags.toolkit.clipboard;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import me.dags.toolkit.Toolkit;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.block.BlockSnapshot;
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

/**
 * @author dags <dags@dags.me>
 */
public class Clipboard {

    private final Transform transform;
    private final Direction direction;
    private final ArchetypeVolume blocks;
    private final Map<EntityArchetype, Vector3d> entities;

    private List<BlockSnapshot> history = new LinkedList<>();

    private Clipboard(ArchetypeVolume volume, Map<EntityArchetype, Vector3d> entities, Direction direction, Transform transform) {
        this.blocks = volume;
        this.entities = entities;
        this.direction = direction;
        this.transform = transform;
    }

    public void apply(Player player, Vector3i position, Cause cause) {
        World world = player.getWorld();
        Direction direction = Utils.direction(Utils.directionVector(player.getRotation()));

        transform.rotate(this.direction, direction);
        transform.setUp();

        history = new LinkedList<>();
        blocks.getBlockWorker(cause).iterate((v, x, y, z) -> transform.apply(world, v, x, y, z, position, history, cause));
    }

    public void undo() {
        for (BlockSnapshot snapshot : history) {
            snapshot.restore(true, BlockChangeFlag.NONE);
        }
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
        Transform transform = Toolkit.getData(player).get("option.wand.select.transform", Transform::new);

        return new Clipboard(blocks, builder.build(), direction, transform);
    }
}
