package net.swofty.type.generic.entity.npc.walk;

/**
 * How an NPC whose position follows quest progress gets to its new spot for one player:
 * walked there in view, walked out of sight and then placed there, or placed there directly.
 */
public enum NPCRelocation {
    TELEPORT,
    WALK,
    DEPART;

    public static final double WATCH_DISTANCE = 48;
    public static final double MAX_WALK_DISTANCE = 128;
    public static final double WALK_BLOCKS_PER_TICK = 0.2;
    public static final double DEPART_BLOCKS_PER_TICK = 0.28;
    public static final int WALK_NODE_BUDGET = 30_000;
    public static final int DEPART_NODE_BUDGET = 8_000;

    public static NPCRelocation decide(boolean walks, double playerToNpc, double travelDistance) {
        if (!walks || playerToNpc > WATCH_DISTANCE || travelDistance < 0.5) return TELEPORT;
        return travelDistance <= MAX_WALK_DISTANCE ? WALK : DEPART;
    }

    /**
     * Whether an NPC on its way should stop walking and appear at the destination: always once it
     * leaves the player's sight, and at the end of a route that does not reach the destination.
     */
    public static boolean vanishes(double playerToNpc, boolean routeReachesDestination, boolean routeFinished) {
        return playerToNpc > WATCH_DISTANCE || (routeFinished && !routeReachesDestination);
    }

    public double blocksPerTick() {
        return this == DEPART ? DEPART_BLOCKS_PER_TICK : WALK_BLOCKS_PER_TICK;
    }

    public int nodeBudget() {
        return this == DEPART ? DEPART_NODE_BUDGET : WALK_NODE_BUDGET;
    }
}
