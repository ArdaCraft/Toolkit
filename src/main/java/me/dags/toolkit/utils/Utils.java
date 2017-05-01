package me.dags.toolkit.utils;

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Utils {

    public static Object flipHinge(BlockTrait<?> trait, Object value) {
        String current = value.toString();
        String hinge = current.equals("left") ? "right" : "left";
        for (Object object : trait.getPossibleValues()) {
            if (object.toString().equals(hinge)) {
                return object;
            }
        }
        return null;
    }

    public static Object flipHalf(BlockTrait<?> trait, Object value) {
        String half = getOppositeHalf(value);
        if (half != null) {
            for (Object object : trait.getPossibleValues()) {
                if (object.toString().equals(half)) {
                    return object;
                }
            }
        }
        return null;
    }

    public static Object flipFacing(BlockTrait<?> trait, Object value, Axis flipAxis) {
        Direction direction = getDirection(value.toString());
        if (direction != null) {
            if (fromDirection(direction) != flipAxis) {
                int angle = clampAngle(getAngle(direction) + 180);
                String facing = getFacing(angle);
                for (Object object : trait.getPossibleValues()) {
                    if (object.toString().equalsIgnoreCase(facing)) {
                        return object;
                    }
                }
            }
        }
        return null;
    }

    public static Object rotateFacing(BlockTrait<?> trait, Object value, int angle) {
        int current = getAngle(value.toString());
        int next = clampAngle(current + angle);
        String facing = getFacing(next);
        for (Object object : trait.getPossibleValues()) {
            if (object.toString().equalsIgnoreCase(facing)) {
                return object;
            }
        }
        return null;
    }

    public static Object rotateAxis(BlockTrait<?> trait, Object value, int angle) {
        String name = value.toString();
        if (!name.equalsIgnoreCase("y") && (angle == 90 || angle == 270)) {
            String axis = name.equals("x") ? "z" : "x";
            for (Object object : trait.getPossibleValues()) {
                if (object.toString().equalsIgnoreCase(axis)) {
                    return object;
                }
            }
        }
        return null;
    }

    public static int getAngle(Direction from, Direction to) {
        int fromAngle = getAngle(from);
        int toAngle = getAngle(to);
        return clampAngle(toAngle - fromAngle);
    }

    public static String getOppositeHalf(Object input) {
        switch (input.toString()) {
            case "bottom":
                return "top";
            case "top":
                return "bottom";
            case "upper":
                return "lower";
            case "lower":
                return "upper";
            default:
                return null;
        }
    }

    public static Axis fromDirection(Direction direction) {
        switch (direction) {
            case EAST:
            case WEST:
                return Axis.X;
            case NORTH:
            case SOUTH:
                return Axis.Z;
            case UP:
            case DOWN:
                return Axis.Y;
            default:
                return null;
        }
    }

    public static String getFacing(int angle) {
        switch (angle) {
            case 90:
                return "east";
            case 180:
                return "south";
            case 270:
                return "west";
            default:
                return "north";
        }
    }

    public static Direction getDirection(String name) {
        switch (name) {
            case "east":
                return Direction.EAST;
            case "south":
                return Direction.SOUTH;
            case "west":
                return Direction.WEST;
            case "north":
                return Direction.NORTH;
            default:
                return null;
        }
    }

    public static int getAngle(String facing) {
        switch (facing) {
            case "east":
                return 90;
            case "south":
                return 180;
            case "west":
                return 270;
            default:
                return 0;
        }
    }

    public static int getAngle(Direction direction) {
        switch (direction) {
            case EAST:
                return 90;
            case SOUTH:
                return 180;
            case WEST:
                return 270;
            default:
                return 0;
        }
    }

    public static int clampAngle(int input) {
        return input < 0 ? 360 + input : input > 360 ? input - 360 : input;
    }

    public static Vector3d directionVector(Vector3d rotation) {
        return Quaterniond.fromAxesAnglesDeg(0, -rotation.getY(), rotation.getZ()).getDirection();
    }

    public static Direction direction(Vector3d direction) {
        return Direction.getClosestHorizontal(direction, Direction.Division.CARDINAL);
    }

    public static void notify(Player player, Object... message) {
        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(message));
    }

    public static void error(Player player, Object... message) {
        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(message).toBuilder().color(TextColors.RED).build());
    }

    public static BlockSnapshot targetBlock(Player player) {
        Optional<BlockRayHit<World>> hit = BlockRay.from(player)
                .stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
                .end();
        return hit.isPresent() ? hit.get().getLocation().createSnapshot() : org.spongepowered.api.block.BlockSnapshot.NONE;
    }

    public static BlockSnapshot targetBlock(Player player, int limit) {
        Optional<BlockRayHit<World>> hit = BlockRay.from(player)
                .stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
                .distanceLimit(limit)
                .end();

        return hit.isPresent() ? hit.get().getLocation().createSnapshot() : org.spongepowered.api.block.BlockSnapshot.NONE;
    }

    public static Vector3i targetPosition(Player player, int limit) {
        Optional<BlockRayHit<World>> hit = BlockRay.from(player)
                .stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
                .distanceLimit(limit)
                .end();

        return hit.isPresent() ? hit.get().getBlockPosition() : Vector3i.ZERO;
    }
}
