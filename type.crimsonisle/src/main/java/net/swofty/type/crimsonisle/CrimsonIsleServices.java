package net.swofty.type.crimsonisle;

import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;
import net.swofty.type.skyblockgeneric.fishing.trophy.GoldenFishService;
import net.swofty.type.skyblockgeneric.fishing.trophy.TrophyFishTabWidget;

/**
 * Everything the Crimson Isle server starts once its world is loaded. Each system adds one line.
 */
public final class CrimsonIsleServices {
    private CrimsonIsleServices() {
    }

    public static void start() {
        VolcanoEruptionService.start();
        GoldenFishService.start();
        TrophyFishTabWidget.register();
    }
}
