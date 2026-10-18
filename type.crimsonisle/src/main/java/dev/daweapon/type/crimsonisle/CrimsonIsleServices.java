package dev.daweapon.type.crimsonisle;

import dev.daweapon.type.crimsonisle.dojo.DojoService;
import dev.daweapon.type.crimsonisle.faction.FactionQuestTicker;
import dev.daweapon.type.crimsonisle.quest.TownBoard;
import dev.daweapon.type.crimsonisle.quest.rescue.RescueService;
import dev.daweapon.type.crimsonisle.town.CrimsonTownServices;
import dev.daweapon.type.skyblockgeneric.crimson.matriarch.MatriarchService;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobSpawner;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMinibossSpawner;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;
import dev.daweapon.type.skyblockgeneric.fishing.trophy.GoldenFishService;

/**
 * Everything the Crimson Isle server starts once its world is loaded. Each system adds one line.
 */
public final class CrimsonIsleServices {
    private CrimsonIsleServices() {
    }

    public static void start() {
        VolcanoEruptionService.start();
        CrimsonMobSpawner.start();
        CrimsonMinibossSpawner.start();
        MatriarchService.start();
        GoldenFishService.start();
        DojoService.start();
        FactionQuestTicker.start();
        TownBoard.spawnAll();
        RescueService.start();
        CrimsonTownServices.start();
    }
}
