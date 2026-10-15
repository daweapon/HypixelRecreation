package dev.daweapon.dungeons.catacombs.run;

public enum DungeonScoreRank {
    D("D", 0),
    C("C", 100),
    B("B", 160),
    A("A", 230),
    S("S", 269.5),
    S_PLUS("S+", 300);

    private final String displayName;
    private final double minimumScore;

    DungeonScoreRank(String displayName, double minimumScore) {
        this.displayName = displayName;
        this.minimumScore = minimumScore;
    }

    public String displayName() {
        return displayName;
    }

    public double minimumScore() {
        return minimumScore;
    }

    public boolean atLeast(DungeonScoreRank other) {
        return compareTo(other) >= 0;
    }

    public static DungeonScoreRank fromScore(double score) {
        DungeonScoreRank result = D;
        for (DungeonScoreRank rank : values()) {
            if (score >= rank.minimumScore) {
                result = rank;
            }
        }
        return result;
    }
}
