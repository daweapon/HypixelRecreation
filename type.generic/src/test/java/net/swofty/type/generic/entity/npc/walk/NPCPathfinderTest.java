package net.swofty.type.generic.entity.npc.walk;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NPCPathfinderTest {
    private static final int GROUND = 63;
    private static final int FLOOR_Y = GROUND + 1;

    private static final class FakeGrid implements WalkGrid {
        private final Map<String, Double> blocks = new HashMap<>();
        private final int min;
        private final int max;

        FakeGrid(int min, int max) {
            this.min = min;
            this.max = max;
            for (int x = min; x <= max; x++) {
                for (int z = min; z <= max; z++) set(x, GROUND, z, 1);
            }
        }

        FakeGrid set(int x, int y, int z, double top) {
            blocks.put(x + "," + y + "," + z, top);
            return this;
        }

        FakeGrid wall(int x, int fromZ, int toZ, int height) {
            for (int z = fromZ; z <= toZ; z++) {
                for (int y = FLOOR_Y; y < FLOOR_Y + height; y++) set(x, y, z, 1);
            }
            return this;
        }

        @Override
        public double top(int x, int y, int z) {
            if (x < min || x > max || z < min || z > max) return BLOCKED;
            return blocks.getOrDefault(x + "," + y + "," + z, 0.0);
        }
    }

    @Test
    void walksStraightAcrossOpenGroundAndEndsOnTheExactSpot() {
        FakeGrid grid = new FakeGrid(-5, 30);
        Pos goal = new Pos(20.5, FLOOR_Y, 0.5, 90, 0);
        NPCPathfinder.Path path = NPCPathfinder.find(grid, new Pos(0.5, FLOOR_Y, 0.5), goal, 5_000);

        assertTrue(path.complete());
        assertEquals(new Vec(20.5, FLOOR_Y, 0.5), path.points().getLast());
        assertTrue(path.points().size() <= 3, "open ground collapses to a straight line: " + path.points());
        assertEquals(20, path.length(), 0.01);
    }

    @Test
    void goesAroundAWallInsteadOfThroughIt() {
        FakeGrid grid = new FakeGrid(-5, 30).wall(10, -5, 20, 3);
        NPCPathfinder.Path path = NPCPathfinder.find(grid, new Pos(5.5, FLOOR_Y, 0.5), new Pos(15.5, FLOOR_Y, 0.5), 20_000);

        assertTrue(path.complete());
        assertNoPointCrosses(path.points(), grid);
        assertTrue(path.length() > 40, "the detour runs past the end of the wall");
    }

    @Test
    void climbsOneBlockButNotTwo() {
        FakeGrid low = new FakeGrid(-5, 20).wall(5, -5, 20, 1);
        NPCPathfinder.Path climb = NPCPathfinder.find(low, new Pos(0.5, FLOOR_Y, 0.5), new Pos(5.5, FLOOR_Y + 1, 0.5), 5_000);
        assertTrue(climb.complete());

        FakeGrid high = new FakeGrid(-5, 20).wall(5, -5, 20, 2);
        NPCPathfinder.Path blocked = NPCPathfinder.find(high, new Pos(0.5, FLOOR_Y, 0.5), new Pos(10.5, FLOOR_Y, 0.5), 5_000);
        assertFalse(blocked.complete());
    }

    @Test
    void dropsUpToThreeBlocks() {
        FakeGrid grid = new FakeGrid(-5, 20);
        for (int x = -5; x <= 4; x++) {
            for (int z = -5; z <= 20; z++) {
                for (int y = FLOOR_Y; y < FLOOR_Y + 3; y++) grid.set(x, y, z, 1);
            }
        }
        NPCPathfinder.Path path = NPCPathfinder.find(grid, new Pos(0.5, FLOOR_Y + 3, 0.5), new Pos(10.5, FLOOR_Y, 0.5), 5_000);
        assertTrue(path.complete());
        assertEquals(FLOOR_Y, path.points().getLast().y());
    }

    @Test
    void neverWadesIntoLiquid() {
        FakeGrid grid = new FakeGrid(-5, 20);
        for (int z = -5; z <= 20; z++) grid.set(5, GROUND, z, WalkGrid.BLOCKED);
        NPCPathfinder.Path path = NPCPathfinder.find(grid, new Pos(0.5, FLOOR_Y, 0.5), new Pos(10.5, FLOOR_Y, 0.5), 5_000);
        assertFalse(path.complete());
    }

    @Test
    void anUnreachableGoalYieldsTheClosestPartialRoute() {
        FakeGrid grid = new FakeGrid(-5, 40).wall(20, -5, 40, 4);
        Pos start = new Pos(0.5, FLOOR_Y, 0.5);
        Pos goal = new Pos(30.5, FLOOR_Y, 0.5);
        NPCPathfinder.Path path = NPCPathfinder.find(grid, start, goal, 50_000);

        assertFalse(path.complete());
        Vec end = path.points().getLast();
        assertTrue(end.distance(goal.asVec()) < start.distance(goal) - 15, "partial route heads towards the goal: " + end);
        assertNoPointCrosses(path.points(), grid);
    }

    @Test
    void standHeightsFollowCollisionShapes() {
        FakeGrid grid = new FakeGrid(-5, 5);
        grid.set(0, FLOOR_Y, 0, 0.5);
        grid.set(1, FLOOR_Y, 0, 0.0625);
        grid.set(2, GROUND, 0, 1.5);
        grid.set(3, FLOOR_Y + 1, 0, 1);

        assertEquals(FLOOR_Y + 0.5, grid.standHeight(0, FLOOR_Y, 0));
        assertEquals(FLOOR_Y + 0.0625, grid.standHeight(1, FLOOR_Y, 0));
        assertTrue(Double.isNaN(grid.standHeight(2, FLOOR_Y, 0)), "fences are not walked on");
        assertTrue(Double.isNaN(grid.standHeight(3, FLOOR_Y, 0)), "no head room under a block");
        assertEquals(FLOOR_Y, grid.standHeight(4, FLOOR_Y, 0));
    }

    private static void assertNoPointCrosses(List<Vec> points, WalkGrid grid) {
        for (int i = 1; i < points.size(); i++) {
            Vec from = points.get(i - 1);
            Vec to = points.get(i);
            int samples = (int) Math.ceil(from.distance(to) / 0.1);
            for (int s = 0; s <= samples; s++) {
                Vec point = from.lerp(to, samples == 0 ? 0 : (double) s / samples);
                int x = (int) Math.floor(point.x());
                int y = (int) Math.floor(point.y() + 0.01);
                int z = (int) Math.floor(point.z());
                assertTrue(grid.passable(x, y, z) || grid.top(x, y, z) <= 0.5,
                        "route passes through a solid block at " + point);
            }
        }
    }
}
