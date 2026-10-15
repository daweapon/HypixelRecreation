package dev.daweapon.type.crimsonisle.dojo.tests;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.crimsonisle.dojo.DojoArena;
import dev.daweapon.type.crimsonisle.dojo.DojoChallenge;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test of Swiftness: a snake of wool blocks crosses the lava. Each new green block turns the last one
 * orange, the orange one red, and the red one disappears; later blocks leave one- and two-block gaps.
 * Eight points for every green block you reach.
 */
public class SwiftnessChallenge extends DojoChallenge {
    private static final int CAGE_TICKS = 3 * TICKS_PER_SECOND;
    private static final int MARGIN = 2;
    private static final Block[] STAGES = {Block.LIME_WOOL, Block.ORANGE_WOOL, Block.RED_WOOL};
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final int cx = (int) Math.floor(DojoArena.CENTER_X);
    private final int cz = (int) Math.floor(DojoArena.CENTER_Z);
    private final Deque<Step> snake = new ArrayDeque<>();
    private final List<int[]> cage = new ArrayList<>();
    private int placed;
    private int[] heading = DIRECTIONS[0];
    private long nextSpawnAt = CAGE_TICKS;

    public SwiftnessChallenge(SkyBlockPlayer player) {
        super(player, DojoTest.SWIFTNESS);
    }

    @Override
    protected void build() {
        arena.floor(cx, cz, Block.STONE_BRICKS);
        snake.add(new Step(cx, cz, true));
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int y = DojoArena.STAND_Y; y <= DojoArena.STAND_Y + 2; y++) {
                    if (dx == 0 && dz == 0) continue;
                    arena.set(cx + dx, y, cz + dz, Block.BARRIER);
                    cage.add(new int[]{cx + dx, y, cz + dz});
                }
            }
        }
    }

    @Override
    protected Pos spawn() {
        return DojoArena.centered(cx, DojoArena.STAND_Y, cz);
    }

    @Override
    protected void tick() {
        if (ticks == CAGE_TICKS) {
            cage.forEach(cell -> arena.restore(cell[0], cell[1], cell[2]));
            cage.clear();
            sound("block.iron_door.open", 1f);
        }
        if (ticks >= nextSpawnAt) {
            advance();
            nextSpawnAt = ticks + Math.max(12, 30 - placed / 3);
        }
        reward();
    }

    private void advance() {
        Step head = snake.peekLast();
        int[] next = nextCell(head);
        for (Step step : snake) step.stage++;
        while (!snake.isEmpty() && snake.peekFirst().stage >= STAGES.length) {
            Step gone = snake.pollFirst();
            arena.restore(gone.x, DojoArena.FLOOR_Y, gone.z);
        }
        snake.forEach(step -> arena.floor(step.x, step.z, STAGES[step.stage]));
        snake.add(new Step(next[0], next[1], false));
        arena.floor(next[0], next[1], STAGES[0]);
        placed++;
        sound("block.wool.place", 1.2f);
    }

    private int[] nextCell(Step head) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int gap = placed < 15 ? 0 : random.nextInt(placed < 40 ? 2 : 3);
        int distance = gap + 1;
        List<int[]> options = new ArrayList<>();
        for (int[] direction : DIRECTIONS) {
            if (direction[0] == -heading[0] && direction[1] == -heading[1]) continue;
            int x = head.x + direction[0] * distance;
            int z = head.z + direction[1] * distance;
            if (x < DojoArena.MIN_X + MARGIN || x > DojoArena.MAX_X - MARGIN
                    || z < DojoArena.MIN_Z + MARGIN || z > DojoArena.MAX_Z - MARGIN) continue;
            if (snake.stream().anyMatch(step -> step.x == x && step.z == z)) continue;
            options.add(direction);
        }
        if (options.isEmpty()) options.add(new int[]{-heading[0], -heading[1]});
        boolean keepHeading = options.stream().anyMatch(o -> o[0] == heading[0] && o[1] == heading[1])
                && random.nextInt(3) > 0;
        if (!keepHeading) heading = options.get(random.nextInt(options.size()));
        return new int[]{head.x + heading[0] * distance, head.z + heading[1] * distance};
    }

    private void reward() {
        if (!player.isOnGround()) return;
        Pos position = player.getPosition();
        int x = (int) Math.floor(position.x());
        int z = (int) Math.floor(position.z());
        for (Step step : snake) {
            if (step.x == x && step.z == z && !step.reached) {
                step.reached = true;
                addScore(8);
                return;
            }
        }
    }

    private static final class Step {
        private final int x;
        private final int z;
        private boolean reached;
        private int stage;

        private Step(int x, int z, boolean reached) {
            this.x = x;
            this.z = z;
            this.reached = reached;
        }
    }
}
