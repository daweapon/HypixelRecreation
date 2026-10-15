package dev.daweapon.type.generic.entity.npc.walk;

/**
 * The terrain an NPC walks over, described per block by the height of its collision top:
 * 0 for open space, up to 1 (or 1.5 for fences and walls) for solid blocks, and a negative value
 * for blocks an NPC must never enter (liquids, unloaded chunks).
 */
public interface WalkGrid {
    double BLOCKED = -1;

    double top(int x, int y, int z);

    default boolean passable(int x, int y, int z) {
        return top(x, y, z) == 0;
    }

    /**
     * The height an NPC stands at with its feet in this block, or NaN when it cannot stand there.
     */
    default double standHeight(int x, int y, int z) {
        double feet = top(x, y, z);
        if (feet < 0 || feet > 0.5 || !passable(x, y + 1, z)) return Double.NaN;
        if (feet > 0) {
            return feet <= 0.2 || passable(x, y + 2, z) ? y + feet : Double.NaN;
        }
        double floor = top(x, y - 1, z);
        return floor >= 0.9 && floor <= 1.0 ? y : Double.NaN;
    }

    default boolean standable(int x, int y, int z) {
        return !Double.isNaN(standHeight(x, y, z));
    }
}
