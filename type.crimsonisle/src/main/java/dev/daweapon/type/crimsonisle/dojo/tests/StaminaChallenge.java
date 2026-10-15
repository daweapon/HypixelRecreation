package dev.daweapon.type.crimsonisle.dojo.tests;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.crimsonisle.dojo.DojoArena;
import dev.daweapon.type.crimsonisle.dojo.DojoChallenge;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoGrade;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoProgress;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test of Stamina: walls sweep across a square platform; step or jump through each wall's hole.
 * Touching a wall or falling ends the test. Nine points per second survived, and a player whose
 * record is already an S starts on 252.
 */
public class StaminaChallenge extends DojoChallenge {
    private static final int HALF = 7;
    private static final int WALL_HEIGHT = 3;
    private static final double POINTS_PER_TICK = 9.0 / TICKS_PER_SECOND;
    private static final int VETERAN_START = 252;
    private static final Block[] DIFFICULTY = {
            Block.WHITE_TERRACOTTA, Block.LIME_TERRACOTTA, Block.YELLOW_TERRACOTTA, Block.ORANGE_TERRACOTTA,
            Block.RED_TERRACOTTA, Block.PURPLE_TERRACOTTA, Block.BLACK_TERRACOTTA};

    private final int cx = (int) Math.floor(DojoArena.CENTER_X);
    private final int cz = (int) Math.floor(DojoArena.CENTER_Z);
    private final List<int[]> wallCells = new ArrayList<>();
    private Wall wall;
    private int wallsPassed;
    private long nextWallAt = 3 * TICKS_PER_SECOND;
    private long nextStepAt;

    public StaminaChallenge(SkyBlockPlayer player) {
        super(player, DojoTest.STAMINA);
        if (DojoProgress.best(player, DojoTest.STAMINA) >= DojoGrade.S.getRequiredScore()) setScore(VETERAN_START);
    }

    @Override
    protected void build() {
        arena.square(HALF, Block.POLISHED_ANDESITE);
    }

    @Override
    protected Pos spawn() {
        return DojoArena.CENTER;
    }

    @Override
    protected void tick() {
        addScore(POINTS_PER_TICK);
        if (wall == null && ticks >= nextWallAt) {
            wall = Wall.random(wallLevel());
            nextStepAt = ticks;
        }
        if (wall != null && ticks >= nextStepAt) step();
        if (touchesWall()) fail();
    }

    @Override
    public String difficulty() {
        int level = wall != null ? wall.level : wallLevel();
        return level < 2 ? "<a>Easy" : level < 4 ? "<e>Medium" : level < 6 ? "<c>Hard" : "<4>Insane";
    }

    private int wallLevel() {
        return Math.min(DIFFICULTY.length - 1, wallsPassed / 4);
    }

    private void step() {
        clearWall();
        wall.position += 1;
        if (wall.position > HALF + 1) {
            wall = null;
            wallsPassed++;
            nextWallAt = ticks + Math.max(6, 24 - wallsPassed);
            return;
        }
        int line = wall.position;
        for (int across = -HALF; across <= HALF; across++) {
            for (int dy = 0; dy < WALL_HEIGHT; dy++) {
                int y = DojoArena.STAND_Y + dy;
                if (wall.isHole(across, y)) continue;
                int[] cell = wall.cell(cx, cz, line, across, y);
                arena.set(cell[0], cell[1], cell[2], DIFFICULTY[wall.level]);
                wallCells.add(cell);
            }
        }
        nextStepAt = ticks + Math.max(3, 9 - wallsPassed / 3);
        sound("block.piston.extend", 0.8f + wall.level * 0.1f);
    }

    private void clearWall() {
        wallCells.forEach(cell -> arena.restore(cell[0], cell[1], cell[2]));
        wallCells.clear();
    }

    private boolean touchesWall() {
        Pos position = player.getPosition();
        int minX = (int) Math.floor(position.x() - 0.3);
        int maxX = (int) Math.floor(position.x() + 0.3);
        int minZ = (int) Math.floor(position.z() - 0.3);
        int maxZ = (int) Math.floor(position.z() + 0.3);
        int minY = (int) Math.floor(position.y());
        int maxY = (int) Math.floor(position.y() + 1.79);
        for (int[] cell : wallCells) {
            if (cell[0] >= minX && cell[0] <= maxX && cell[2] >= minZ && cell[2] <= maxZ
                    && cell[1] >= minY && cell[1] <= maxY) return true;
        }
        return false;
    }

    @Override
    public void cleanup() {
        clearWall();
        super.cleanup();
    }

    private static final class Wall {
        private final boolean alongX;
        private final boolean reversed;
        private final int holeStart;
        private final int holeWidth;
        private final int holeBottom;
        private final int level;
        private int position = -HALF - 2;

        private Wall(boolean alongX, boolean reversed, int holeStart, int holeWidth, int holeBottom, int level) {
            this.alongX = alongX;
            this.reversed = reversed;
            this.holeStart = holeStart;
            this.holeWidth = holeWidth;
            this.holeBottom = holeBottom;
            this.level = level;
        }

        /** Later walls narrow the hole to one block and sometimes lift it so it must be jumped through. */
        private static Wall random(int level) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int width = level >= 2 ? 1 : 2;
            int start = random.nextInt(-HALF, HALF - width + 2);
            boolean raised = level >= 3 && random.nextInt(3) == 0;
            return new Wall(random.nextBoolean(), random.nextBoolean(), start, width,
                    DojoArena.STAND_Y + (raised ? 1 : 0), level);
        }

        private boolean isHole(int across, int y) {
            return across >= holeStart && across < holeStart + holeWidth && y >= holeBottom && y < holeBottom + 2;
        }

        private int[] cell(int cx, int cz, int line, int across, int y) {
            int along = reversed ? -line : line;
            return alongX ? new int[]{cx + along, y, cz + across} : new int[]{cx + across, y, cz + along};
        }
    }
}
