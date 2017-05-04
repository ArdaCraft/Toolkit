package me.dags.toolkit.clipboard;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.util.concurrent.FutureCallback;
import me.dags.commandbus.format.FMT;
import me.dags.toolkit.Toolkit;
import me.dags.toolkit.clipboard.block.Facing;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;
import org.spongepowered.api.world.storage.WorldProperties;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Clipboard {

    private final Facing facing;
    private final Vector3i origin;
    private final BlockVolume source;
    private final History history = new History(5);

    private Clipboard(BlockVolume source, Vector3i origin, Facing facing) {
        this.source = source.getImmutableBlockCopy();
        this.origin = origin;
        this.facing = facing;
    }

    public void paste(Player player, Vector3i position, Cause cause) {
        final World world = player.getWorld();
        final UUID uuid = player.getUniqueId();

        ClipboardOptions options = Toolkit.getData(player).get("options", ClipboardOptions::new);
        options.setClipboardFacing(facing);
        options.setPlayerFacing(player);

        FutureCallback<BlockVolume> callback = new FutureCallback<BlockVolume>() {
            @Override
            public void onSuccess(@Nullable BlockVolume result) {
                paste(world, result, position.add(origin), cause, uuid);
            }

            @Override
            public void onFailure(Throwable t) {
                FMT.warn("Unable to transform the clipboard! See the console").tell(player);
                t.printStackTrace();
            }
        };

        Transform transform = options.createTransform();
        Transform.Task task = transform.createTask(source, cause, callback);
        Toolkit.submitAsyncTask(task);
    }

    public void undo(Player player) {
        if (history.hasNext()) {
            List<BlockSnapshot> record = history.popRecord();
            Utils.notify(player, "Undo (", history.getSize(), "/", history.getMax(), ")");
            for (BlockSnapshot snapshot : record) {
                snapshot.restore(true, BlockChangeFlag.NONE);
            }
        } else {
            Utils.error(player, "No more history to undo");
        }
    }

    private void paste(World world, BlockVolume source, Vector3i position, Cause cause, UUID uuid) {
        final List<Transaction<BlockSnapshot>> transactions = new LinkedList<>();
        final WorldProperties properties = world.getProperties();

        source.getBlockWorker(cause).iterate((volume, x, y, z) -> {
            BlockState state = volume.getBlock(x, y, z);
            if (state.getType() == BlockTypes.AIR) {
                return;
            }

            x += position.getX();
            y += position.getY();
            z += position.getZ();

            if (!world.containsBlock(x, y, z)) {
                return;
            }

            BlockSnapshot from = world.createSnapshot(x, y, z);

            BlockSnapshot to = BlockSnapshot.builder()
                    .position(new Vector3i(x, y, z))
                    .world(properties)
                    .blockState(state)
                    .notifier(uuid)
                    .creator(uuid)
                    .build();

            transactions.add(new Transaction<>(from, to));
        });

        ChangeBlockEvent.Place test = SpongeEventFactory.createChangeBlockEventPlace(cause, world, transactions);
        Sponge.getEventManager().post(test);

        if (!test.isCancelled()) {
            List<BlockSnapshot> records = history.nextRecord();

            test.getTransactions().stream()
                    .filter(Transaction::isValid)
                    .peek(transaction -> records.add(transaction.getOriginal()))
                    .forEach(transaction -> transaction.getFinal().restore(true, BlockChangeFlag.NONE));
        }
    }

    public static Clipboard of(Player player, Vector3i min, Vector3i max, Vector3i origin) {
        BlockVolume source = player.getWorld().getBlockView(min, max).getRelativeBlockView();
        Facing facing = Facing.horizontalFacing(player);
        return new Clipboard(source, origin, facing);
    }
}
