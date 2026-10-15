package dev.daweapon.dungeons.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;

public record DungeonScoreInput(
        CatacombsFloor floor,
        CatacombsMode mode,
        int deaths,
        boolean spiritPetReduced,
        int failedPuzzles,
        int clearedRooms,
        int totalRooms,
        int secretsFound,
        int totalSecrets,
        long totalSeconds,
        int crypts,
        boolean mimicKilled,
        boolean paulBonus,
        boolean princeAttribute,
        boolean batAttribute,
        boolean bossDefeated
) {
}
