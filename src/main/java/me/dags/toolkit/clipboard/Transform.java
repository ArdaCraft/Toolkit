package me.dags.toolkit.clipboard;

import com.flowpowered.math.vector.Vector3i;
import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.BlockVolume;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * @author dags <dags@dags.me>
 */
public class Transform {

    private static final Random RANDOM = new Random();

    private int angle = 0;
    private double rads = 0;

    private boolean rotate = true;
    private boolean flipX = false;
    private boolean flipY = false;
    private boolean flipZ = false;

    private boolean randomHorizontal = false;
    private boolean randomVertical = false;
    private boolean randomRotate = false;

    public Transform rotate(int angle) {
        this.angle = angle;
        this.rads = Math.toRadians(angle);
        return this;
    }

    public Transform reset() {
        rotate(0);
        flipX(false);
        flipY(false);
        flipZ(false);
        randomRotate(false);
        randomVertical(false);
        randomHorizontal(false);
        return this;
    }

    public Transform rotate(Direction from, Direction to) {
        rotate(rotate ? Utils.getAngle(from, to) : 0);
        return this;
    }

    public Transform rotate(boolean rotate) {
        this.rotate = rotate;
        return this;
    }

    public Transform flipX(boolean flip) {
        this.flipX = flip;
        return this;
    }

    public Transform flipY(boolean flip) {
        this.flipY = flip;
        return this;
    }

    public Transform flipZ(boolean flip) {
        this.flipZ = flip;
        return this;
    }

    public Transform randomHorizontal(boolean randomHorizontal) {
        this.randomHorizontal = randomHorizontal;
        return this;
    }

    public Transform randomVertical(boolean randomVertical) {
        this.randomVertical = randomVertical;
        return this;
    }

    public Transform randomRotate(boolean randomRotate) {
        this.randomRotate = randomRotate;
        return this;
    }

    public Transform setUp() {
        if (randomRotate) {
            rotate(RANDOM.nextInt(4) * 90);
        }
        if (randomHorizontal) {
            flipX = RANDOM.nextBoolean();
        }
        if (randomHorizontal) {
            flipZ = RANDOM.nextBoolean();
        }
        if (randomVertical) {
            flipY = RANDOM.nextBoolean();
        }
        return this;
    }

    public void apply(World world, BlockVolume source, int x, int y, int z, Vector3i pos, List<BlockSnapshot> history, Cause cause) {
        BlockState state = source.getBlock(x, y, z);
        if (state.getType() == BlockTypes.AIR) {
            return;
        }

        if (angle != 0) {
            int rx = rotateY(x, z, rads, -1);
            int rz = rotateY(z, x, rads, 1);
            x = rx;
            z = rz;
            state = rotateFacing(state, angle);
            state = rotateAxis(state, angle);
        }

        if (flipY) {
            y = -y;
            state = flipHalf(state);
        }

        if (flipX) {
            z = -z;
            state = flipFacing(state, Axis.X);
            state = flipHinge(state);
        }

        if (flipZ) {
            x = -x;
            state = flipFacing(state, Axis.Z);
            state = flipHinge(state);
        }

        x += pos.getX();
        y += pos.getY();
        z += pos.getZ();

        history.add(world.createSnapshot(x, y, z));
        world.setBlock(x, y, z, state, BlockChangeFlag.NONE, cause);
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

    private static BlockState flipFacing(BlockState state, Axis axis) {
        Optional<BlockTrait<?>> facing = state.getTrait("facing");
        if (facing.isPresent()) {
            Optional<?> val = state.getTraitValue(facing.get());
            if (val.isPresent()) {
                Object flipped = Utils.flipFacing(facing.get(), val.get(), axis);
                if (flipped != null) {
                    return state.withTrait(facing.get(), flipped).orElse(state);
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

    private static BlockState flipHinge(BlockState state) {
        Optional<BlockTrait<?>> hinge = state.getTrait("hinge");
        if (hinge.isPresent()) {
            Optional<?> val = state.getTraitValue(hinge.get());
            if (val.isPresent()) {
                Object flipped = Utils.flipHinge(hinge.get(), val.get());
                if (flipped != null) {
                    return state.withTrait(hinge.get(), flipped).orElse(state);
                }
            }
        }
        return state;
    }

    private static BlockState flipHalf(BlockState state) {
        Optional<BlockTrait<?>> half = state.getTrait("half");
        if (half.isPresent()) {
            Optional<?> val = state.getTraitValue(half.get());
            if (val.isPresent()) {
                Object flipped = Utils.flipHalf(half.get(), val.get());
                if (flipped != null) {
                    state = state.withTrait(half.get(), flipped).orElse(state);

                    // doors are weird...
                    if (state.getTrait("hinge").isPresent()) {
                        state = rotateFacing(state, 90);
                    }
                }
            }
        }
        return state;
    }
}
