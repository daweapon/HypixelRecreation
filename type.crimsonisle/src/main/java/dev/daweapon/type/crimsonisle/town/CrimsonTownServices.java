package dev.daweapon.type.crimsonisle.town;

import dev.daweapon.type.skyblockgeneric.crimson.town.CrimsonTownProgress;

/** Startup for the remaining town NPCs: the walkers' timers, Nuvian's keypad and Bruuh's fairy-soul toll. */
public final class CrimsonTownServices {
    private CrimsonTownServices() {
    }

    public static void start() {
        Roamers.start();
        NuvianKeypad.start();
        CrimsonTownProgress.registerGates();
    }
}
