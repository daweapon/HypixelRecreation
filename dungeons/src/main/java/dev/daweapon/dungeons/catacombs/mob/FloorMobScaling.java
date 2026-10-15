package dev.daweapon.dungeons.catacombs.mob;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;

public record FloorMobScaling(
        String mobId,
        CatacombsFloor floor,
        long health,
        long damage,
        int defense,
        String source
) {
    public static final String WIKI_RANGE_MINIMUM =
            "ESTIMATE: lowest value of the Dungeon Mobs wiki range; the wiki has no per-floor value";
    private static final double HEALTH_DAMAGE_STEP_PER_ROOM = 0.05;

    public static double roomsExploredMultiplier(int roomsExploredBefore) {
        return 1 + HEALTH_DAMAGE_STEP_PER_ROOM * Math.max(0, roomsExploredBefore - 1);
    }

    public boolean estimated() {
        return source.startsWith("ESTIMATE");
    }
}
