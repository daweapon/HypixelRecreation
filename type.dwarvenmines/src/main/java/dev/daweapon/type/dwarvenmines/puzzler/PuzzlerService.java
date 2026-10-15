package dev.daweapon.type.dwarvenmines.puzzler;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class PuzzlerService {
    public static final long REWARD_POWDER = 2_500;
    public static final long COOLDOWN_MILLIS = 24L * 60L * 60L * 1000L;
    private static final int SEARCH_RADIUS = 12;
    private static final int MIN_MOVES = 5;
    private static final int MAX_MOVES = 8;
    private static final int ATTEMPTS = 60;

    private static final Map<UUID, Puzzle> ACTIVE = new ConcurrentHashMap<>();

    public record Puzzle(Point start, Point target, String arrows) {}

    private enum Arrow {
        FORWARD("▲", 0, 1),
        BACK("▼", 0, -1),
        RIGHT("▶", -1, 0),
        LEFT("◀", 1, 0);

        private final String symbol;
        private final int dx;
        private final int dz;

        Arrow(String symbol, int dx, int dz) {
            this.symbol = symbol;
            this.dx = dx;
            this.dz = dz;
        }
    }

    private PuzzlerService() {
    }

    public static long millisUntilReady(SkyBlockPlayer player) {
        return Math.max(0, HotmService.data(player).getPuzzlerReadyAt() - System.currentTimeMillis());
    }

    public static Puzzle active(SkyBlockPlayer player) {
        return ACTIVE.get(player.getUuid());
    }

    public static Puzzle generate(SkyBlockPlayer player, Pos puzzlerPosition) {
        Instance instance = HypixelConst.getInstanceContainer();
        Point start = findGlowstone(instance, puzzlerPosition);
        if (start == null) return null;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int moves = random.nextInt(MIN_MOVES, MAX_MOVES + 1);
            StringBuilder arrows = new StringBuilder();
            int dx = 0;
            int dz = 0;
            for (int move = 0; move < moves; move++) {
                Arrow arrow = Arrow.values()[random.nextInt(Arrow.values().length)];
                arrows.append(arrow.symbol);
                dx += arrow.dx;
                dz += arrow.dz;
            }
            if (dx == 0 && dz == 0) continue;
            Point target = start.add(dx, 0, dz);
            Block block = instance.getBlock(target);
            if (block.isAir() || block.compare(Block.GLOWSTONE)) continue;
            Puzzle puzzle = new Puzzle(start, target, arrows.toString());
            ACTIVE.put(player.getUuid(), puzzle);
            return puzzle;
        }
        return null;
    }

    private static Point findGlowstone(Instance instance, Pos center) {
        Point best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    Point point = new Vec(center.blockX() + x, center.blockY() + y, center.blockZ() + z);
                    if (!instance.getBlock(point).compare(Block.GLOWSTONE)) continue;
                    double distance = point.distanceSquared(center);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = point;
                    }
                }
            }
        }
        return best;
    }

    public static boolean isInPuzzleArea(Puzzle puzzle, Point broken) {
        return Math.abs(broken.blockX() - puzzle.start().blockX()) <= MAX_MOVES + 1
                && Math.abs(broken.blockZ() - puzzle.start().blockZ()) <= MAX_MOVES + 1
                && Math.abs(broken.blockY() - puzzle.start().blockY()) <= 2;
    }

    public static boolean isTarget(Puzzle puzzle, Point broken) {
        return broken.blockX() == puzzle.target().blockX()
                && broken.blockY() == puzzle.target().blockY()
                && broken.blockZ() == puzzle.target().blockZ();
    }

    public static long solve(SkyBlockPlayer player) {
        ACTIVE.remove(player.getUuid());
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        data.setPuzzlerReadyAt(System.currentTimeMillis() + COOLDOWN_MILLIS);
        HotmService.save(player, data);
        return HotmService.gainPowder(player, HotmPowder.MITHRIL, REWARD_POWDER);
    }

    public static void clear(SkyBlockPlayer player) {
        ACTIVE.remove(player.getUuid());
    }

}
