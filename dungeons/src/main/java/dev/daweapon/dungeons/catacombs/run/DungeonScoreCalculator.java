package dev.daweapon.dungeons.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DungeonScoreCalculator {
    public static final int MAXIMUM_SCORE = 300;
    public static final int MINIMUM_SKILL = 20;
    public static final int LEAVE_PENALTY_PERCENT = 30;
    private static final int DEATH_PENALTY = 2;
    private static final int FAILED_PUZZLE_PENALTY = 14;
    private static final int MAXIMUM_CRYPT_BONUS = 5;
    private static final int MIMIC_BONUS = 2;
    private static final int PAUL_BONUS = 10;
    private static final int ATTRIBUTE_BONUS = 1;

    public static DungeonScoreBreakdown calculate(DungeonScoreInput input) {
        int skill = skill(input);
        int exploration = exploration(input);
        int speed = speed(input);
        int bonus = bonus(input);
        int total = Math.min(MAXIMUM_SCORE, skill + exploration + speed + bonus);
        int penalty = input.bossDefeated() ? 0 : total * LEAVE_PENALTY_PERCENT / 100;
        return new DungeonScoreBreakdown(skill, exploration, speed, bonus, penalty);
    }

    public static int skill(DungeonScoreInput input) {
        int deathPenalty = input.deaths() * DEATH_PENALTY;
        if (input.spiritPetReduced() && input.deaths() > 0) {
            deathPenalty--;
        }
        int raw = 100 - deathPenalty - input.failedPuzzles() * FAILED_PUZZLE_PENALTY;
        return clamp(raw, MINIMUM_SKILL, 100);
    }

    public static int exploration(DungeonScoreInput input) {
        int rooms = input.totalRooms() <= 0
                ? 60
                : 60 * clamp(input.clearedRooms(), 0, input.totalRooms()) / input.totalRooms();
        int requirement = DungeonScoreTables.secretRequirementPercent(input.floor(), input.mode());
        long requiredSecretUnits = (long) requirement * input.totalSecrets();
        int secrets = requiredSecretUnits <= 0
                ? 40
                : (int) Math.min(40, 4000L * Math.max(0, input.secretsFound()) / requiredSecretUnits);
        return rooms + secrets;
    }

    public static int speed(DungeonScoreInput input) {
        if (!input.bossDefeated()) {
            return input.totalRooms() <= 0
                    ? 0
                    : 100 * clamp(input.clearedRooms(), 0, input.totalRooms()) / input.totalRooms();
        }
        long adjusted = Math.max(0, input.totalSeconds()
                - DungeonScoreTables.timeOffsetSeconds(input.floor(), input.mode()));
        return clamp(speedForAdjustedSeconds(adjusted), 0, 100);
    }

    public static int speedForAdjustedSeconds(long adjustedSeconds) {
        if (adjustedSeconds < 480) {
            return 100;
        }
        if (adjustedSeconds < 600) {
            return (int) (140 - adjustedSeconds / 12);
        }
        if (adjustedSeconds < 840) {
            return (int) (115 - adjustedSeconds / 24);
        }
        if (adjustedSeconds < 1140) {
            return (int) (108 - adjustedSeconds / 30);
        }
        if (adjustedSeconds < 3940) {
            return (int) Math.ceilDiv(3940 - adjustedSeconds, 40L);
        }
        return 0;
    }

    public static int bonus(DungeonScoreInput input) {
        int bonus = Math.min(MAXIMUM_CRYPT_BONUS, Math.max(0, input.crypts()));
        if (input.mimicKilled() && input.floor().ordinal() >= CatacombsFloor.FLOOR_SIX.ordinal()) {
            bonus += MIMIC_BONUS;
        }
        if (input.paulBonus()) {
            bonus += PAUL_BONUS;
        }
        if (input.princeAttribute()) {
            bonus += ATTRIBUTE_BONUS;
        }
        if (input.batAttribute()) {
            bonus += ATTRIBUTE_BONUS;
        }
        return bonus;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
