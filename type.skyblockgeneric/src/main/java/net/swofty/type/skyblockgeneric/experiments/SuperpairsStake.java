package net.swofty.type.skyblockgeneric.experiments;

public enum SuperpairsStake {
    BEGINNER("Beginner", "<b>", 7, 2, 10, 10, 25, 50_000, 3_500, 50_000),
    HIGH("High", "<a>", 5, 4, 12, 20, 50, 75_000, 5_000, 60_000),
    GRAND("Grand", "<e>", 5, 4, 12, 25, 75, 100_000, 5_000, 90_000),
    SUPREME("Supreme", "<6>", 7, 4, 14, 30, 100, 200_000, 5_000, 120_000),
    TRANSCENDENT("Transcendent", "<c>", 7, 4, 16, 40, 200, 300_000, 25_000, 130_000),
    METAPHYSICAL("Metaphysical", "<d>", 7, 4, 12, 50, 350, 400_000, 25_000, 150_000);

    public static final int TIME_LIMIT_SECONDS = 90;

    private final String displayName;
    private final String color;
    private final int columns;
    private final int rows;
    private final int baseClicks;
    private final int requiredEnchanting;
    private final int startLevels;
    private final long stakeXp;
    private final long pairXpMin;
    private final long pairXpMax;

    SuperpairsStake(String displayName, String color, int columns, int rows, int baseClicks, int requiredEnchanting, int startLevels,
                    long stakeXp, long pairXpMin, long pairXpMax) {
        this.displayName = displayName;
        this.color = color;
        this.columns = columns;
        this.rows = rows;
        this.baseClicks = baseClicks;
        this.requiredEnchanting = requiredEnchanting;
        this.startLevels = startLevels;
        this.stakeXp = stakeXp;
        this.pairXpMin = pairXpMin;
        this.pairXpMax = pairXpMax;
    }

    public String displayName() {
        return displayName;
    }

    public String color() {
        return color;
    }

    public int baseClicks() {
        return baseClicks;
    }

    public int requiredEnchanting() {
        return requiredEnchanting;
    }

    public int startLevels() {
        return startLevels;
    }

    public long stakeXp() {
        return stakeXp;
    }

    public long pairXpMin() {
        return pairXpMin;
    }

    public long pairXpMax() {
        return pairXpMax;
    }

    public String gridLabel() {
        return columns + "x" + rows;
    }

    public int tileCount() {
        return columns * rows;
    }

    public int pairCount() {
        return tileCount() / 2;
    }

    public int addOnMultiplier() {
        return this == METAPHYSICAL ? 2 : 1;
    }

    public int slotOf(int tile) {
        int firstRow = rows == 2 ? 2 : 1;
        int firstColumn = (9 - columns) / 2;
        return (firstRow + tile / columns) * 9 + firstColumn + tile % columns;
    }

    public int tileOfSlot(int slot) {
        for (int tile = 0; tile < tileCount(); tile++) {
            if (slotOf(tile) == slot) return tile;
        }
        return -1;
    }
}
