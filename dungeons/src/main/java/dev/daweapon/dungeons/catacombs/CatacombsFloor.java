package dev.daweapon.dungeons.catacombs;

import java.util.Arrays;

// Catacombs floors (Entrance through Floor VII) with their boss, size, and Normal/Master score requirements.
public enum CatacombsFloor {
    ENTRANCE("Entrance", "The Watcher", CatacombsDungeonSize.FOUR_BY_FOUR, 0, -1),
    FLOOR_ONE("Floor I", "Bonzo", CatacombsDungeonSize.FOUR_BY_FIVE, 1, 24),
    FLOOR_TWO("Floor II", "Scarf", CatacombsDungeonSize.FIVE_BY_FIVE, 3, 26),
    FLOOR_THREE("Floor III", "The Professor", CatacombsDungeonSize.FIVE_BY_FIVE, 5, 28),
    FLOOR_FOUR("Floor IV", "Thorn", CatacombsDungeonSize.SIX_BY_FIVE, 9, 30),
    FLOOR_FIVE("Floor V", "Livid", CatacombsDungeonSize.SIX_BY_FIVE, 14, 32),
    FLOOR_SIX("Floor VI", "Sadan", CatacombsDungeonSize.SIX_BY_SIX, 19, 34),
    FLOOR_SEVEN("Floor VII", "The Wither Lords", CatacombsDungeonSize.SIX_BY_SIX, 24, 36);

    private final String displayName;
    private final String bossName;
    private final CatacombsDungeonSize dungeonSize;
    private final int normalRequirement;
    private final int masterRequirement;

    CatacombsFloor(String displayName, String bossName, CatacombsDungeonSize dungeonSize,
                   int normalRequirement, int masterRequirement) {
        this.displayName = displayName;
        this.bossName = bossName;
        this.dungeonSize = dungeonSize;
        this.normalRequirement = normalRequirement;
        this.masterRequirement = masterRequirement;
    }

    public String displayName() {
        return displayName;
    }

    public String bossName() {
        return bossName;
    }

    public CatacombsDungeonSize dungeonSize() {
        return dungeonSize;
    }

    public int normalRequirement() {
        return normalRequirement;
    }

    public int masterRequirement() {
        return masterRequirement;
    }

    public boolean supports(CatacombsMode mode) {
        return mode == CatacombsMode.NORMAL || masterRequirement >= 0;
    }

    public int requirement(CatacombsMode mode) {
        if (mode == CatacombsMode.MASTER && masterRequirement < 0) {
            throw new IllegalArgumentException(displayName + " does not support Master Mode");
        }
        return mode == CatacombsMode.MASTER ? masterRequirement : normalRequirement;
    }

    public static CatacombsFloor byNumber(int floor) {
        return Arrays.stream(values())
                .filter(value -> value.ordinal() == floor)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Catacombs floor " + floor));
    }
}
