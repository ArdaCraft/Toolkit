package me.dags.toolkit.clipboard;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.util.concurrent.FutureCallback;
import me.dags.toolkit.Toolkit;
import me.dags.toolkit.clipboard.block.Axis;
import me.dags.toolkit.clipboard.block.BlockStateUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.extent.BlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;

/**
 * @author dags <dags@dags.me>
 */
public class Transform {

    private final int angle;
    private final double radians;
    private final boolean flipX;
    private final boolean flipY;
    private final boolean flipZ;

    Transform(int angle, boolean x, boolean y, boolean z) {
        this.angle = angle;
        this.radians = Math.toRadians(angle);
        this.flipX = x;
        this.flipY = y;
        this.flipZ = z;
    }

    public Task createTask(BlockVolume source, Cause cause, FutureCallback<BlockVolume> callback) {
        return new Task(cause, source, callback);
    }

    public MutableBlockVolume apply(BlockVolume source, Cause cause) {
        Vector3i pos1 = applyToVector(source.getBlockMin());
        Vector3i pos2 = applyToVector(source.getBlockMax());
        Vector3i size = pos1.max(pos2).sub(pos1.min(pos2)).add(Vector3i.ONE);
        MutableBlockVolume buffer = Sponge.getRegistry().getExtentBufferFactory().createBlockBuffer(size);
        source.getBlockWorker(cause).iterate((v, x, y, z) -> visit(v, buffer, x, y, z, cause));
        return buffer;
    }

    private void visit(BlockVolume src, MutableBlockVolume buffer, int x, int y, int z, Cause cause) {
        BlockState state = src.getBlock(x, y, z);
        if (state.getType() == BlockTypes.AIR) {
            return;
        }

        if (angle != 0) {
            int rx = rotateY(x, z, radians, -1);
            int rz = rotateY(z, x, radians, 1);
            x = rx;
            z = rz;
            state = BlockStateUtil.rotateFacing(state, Axis.y, angle);
            state = BlockStateUtil.rotateAxis(state, Axis.y, angle);
        }

        if (flipX) {
            x = -x;
            state = BlockStateUtil.flipFacing(state, Axis.x);
            state = BlockStateUtil.flipHinge(state, Axis.x);
        }

        if (flipY) {
            y = -y;
            state = BlockStateUtil.flipHalf(state, Axis.y);
            state = BlockStateUtil.flipFacing(state, Axis.y);
        }

        if (flipZ) {
            z = -z;
            state = BlockStateUtil.flipFacing(state, Axis.z);
            state = BlockStateUtil.flipHinge(state, Axis.z);
        }

        if (buffer.containsBlock(x, y, z)) {
            buffer.setBlock(x, y, z, state, cause);
        }
    }

    private Vector3i applyToVector(Vector3i pos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();

        if (angle != 0) {
            int rx = rotateY(x, z, radians, -1);
            int rz = rotateY(z, x, radians, 1);
            x = rx;
            z = rz;
        }

        if (flipY) {
            y = -y;
        }

        if (flipX) {
            z = -z;
        }

        if (flipZ) {
            x = -x;
        }

        return new Vector3i(x, y, z);
    }

    private static int rotateY(int a, int b, double rads, int sign) {
        return (int) Math.round(a * Math.cos(rads) + (sign * b) * Math.sin(rads));
    }

    public class Task implements Runnable {

        private final Cause cause;
        private final BlockVolume source;
        private final FutureCallback<BlockVolume> callback;

        private Task(Cause cause, BlockVolume source, FutureCallback<BlockVolume> callback) {
            this.cause = cause;
            this.source = source;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                BlockVolume transformed = apply(source, cause);
                Toolkit.submitSyncTask(() -> callback.onSuccess(transformed));
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }
    }
}
