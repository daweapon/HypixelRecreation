package dev.daweapon.dungeons.catacombs.run;

public record DungeonScoreBreakdown(
        int skill,
        int exploration,
        int speed,
        int bonus,
        int leavePenalty
) {
    public DungeonScoreBreakdown(int skill, int exploration, int speed, int bonus) {
        this(skill, exploration, speed, bonus, 0);
    }

    public int total() {
        return Math.min(DungeonScoreCalculator.MAXIMUM_SCORE, skill + exploration + speed + bonus) - leavePenalty;
    }

    public DungeonScoreRank rank() {
        return DungeonScoreRank.fromScore(total());
    }
}
