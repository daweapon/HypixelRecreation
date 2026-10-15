package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreBreakdown;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreCalculator;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreInput;

public final class CatacombsScoring {
    private CatacombsScoring() {
    }

    /** A player who left before the boss fell is scored as if the boss was never defeated, which costs 30%. */
    public static DungeonScoreBreakdown forPlayer(CatacombsRunState state, boolean leftEarly) {
        DungeonScoreInput input = state.scoreInput();
        if (!leftEarly || !input.bossDefeated()) return DungeonScoreCalculator.calculate(input);
        return DungeonScoreCalculator.calculate(new DungeonScoreInput(input.floor(), input.mode(), input.deaths(),
                input.spiritPetReduced(), input.failedPuzzles(), input.clearedRooms(), input.totalRooms(),
                input.secretsFound(), input.totalSecrets(), input.totalSeconds(), input.crypts(), input.mimicKilled(),
                input.paulBonus(), input.princeAttribute(), input.batAttribute(), false));
    }
}
