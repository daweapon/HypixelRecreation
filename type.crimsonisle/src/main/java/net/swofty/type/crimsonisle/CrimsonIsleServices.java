package net.swofty.type.crimsonisle;

import net.swofty.type.crimsonisle.dojo.DojoService;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;

/**
 * Everything the Crimson Isle server starts once its world is loaded. Each system adds one line.
 */
public final class CrimsonIsleServices {
    private CrimsonIsleServices() {
    }

    public static void start() {
        VolcanoEruptionService.start();
        DojoService.start();
    }
}
