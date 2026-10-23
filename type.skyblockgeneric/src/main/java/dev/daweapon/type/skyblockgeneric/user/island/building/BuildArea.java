package dev.daweapon.type.skyblockgeneric.user.island.building;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.communityupgrades.CommunityUpgrades;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.tag.Tag;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class BuildArea {
    public static final Tag<Boolean> PLAYER_PLACED = Tag.Boolean("player_placed");
    public static final int MAX_BUILD_HEIGHT = 296;
    private static final int EXPLORATION_CAP = 4096;

    private BuildArea() {
    }

    public static boolean onOwnIsland(SkyBlockPlayer player) {
        SkyBlockIsland island = SkyBlockIsland.forInstance(player.getInstance());
        return HypixelConst.isIslandServer() && island != null && island == player.getSkyBlockIsland();
    }

    public static boolean withinIsland(SkyBlockPlayer player, Point point) {
        int half = (int) Math.floor((double) CommunityUpgrades.islandSize(player) / 2);
        return Math.abs(point.blockX()) <= half && Math.abs(point.blockZ()) <= half && point.blockY() <= MAX_BUILD_HEIGHT;
    }

    public static boolean isPlayerPlaced(Block block) {
        return Boolean.TRUE.equals(block.getTag(PLAYER_PLACED));
    }

    public static Vec offset(BlockFace face) {
        return switch (face) {
            case NORTH -> new Vec(0, 0, -1);
            case SOUTH -> new Vec(0, 0, 1);
            case EAST -> new Vec(1, 0, 0);
            case WEST -> new Vec(-1, 0, 0);
            case TOP -> new Vec(0, 1, 0);
            case BOTTOM -> new Vec(0, -1, 0);
        };
    }

    public static List<Point> facePlane(Instance instance, Point origin, BlockFace face, boolean diagonals, Predicate<Point> accepts) {
        List<Vec> steps = planeSteps(face, diagonals);
        Set<Point> seen = new HashSet<>();
        ArrayDeque<Point> queue = new ArrayDeque<>();
        List<Point> found = new ArrayList<>();
        Point start = new Vec(origin.blockX(), origin.blockY(), origin.blockZ());
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty() && found.size() < EXPLORATION_CAP) {
            Point current = queue.poll();
            if (!accepts.test(current)) continue;
            found.add(current);
            for (Vec step : steps) {
                Point next = current.add(step);
                if (seen.add(next)) queue.add(next);
            }
        }
        return found;
    }

    private static List<Vec> planeSteps(BlockFace face, boolean diagonals) {
        List<Vec> steps = new ArrayList<>();
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) continue;
                if (!diagonals && a != 0 && b != 0) continue;
                steps.add(switch (face) {
                    case NORTH, SOUTH -> new Vec(a, b, 0);
                    case EAST, WEST -> new Vec(0, a, b);
                    case TOP, BOTTOM -> new Vec(a, 0, b);
                });
            }
        }
        return steps;
    }
}
