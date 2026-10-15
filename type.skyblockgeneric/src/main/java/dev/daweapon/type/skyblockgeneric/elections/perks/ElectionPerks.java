package dev.daweapon.type.skyblockgeneric.elections.perks;

import dev.daweapon.type.skyblockgeneric.elections.ElectionData;
import dev.daweapon.type.skyblockgeneric.elections.ElectionManager;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor.Perk;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The one place gameplay code asks whether a Mayor, Minister or Perkpocalypse perk is in effect.
 * The answer is cached until the election state or the Perkpocalypse slot changes, so it is safe
 * to call on every stat recalculation or block break.
 */
public final class ElectionPerks {
    private static volatile ActivePerks cached = ActivePerks.NONE;

    private ElectionPerks() {}

    public static ActivePerks current() {
        ElectionData data = ElectionManager.getElectionData();
        SkyBlockMayor perkpocalypse = ElectionManager.getPerkpocalypseSourceMayor();
        ActivePerks snapshot = cached;
        if (snapshot.reflects(data, perkpocalypse)) return snapshot;
        snapshot = ActivePerks.of(data, perkpocalypse);
        cached = snapshot;
        return snapshot;
    }

    public static boolean isActive(Perk perk) {
        return current().isActive(perk);
    }

    public static double bonus(Perk perk) {
        return current().bonus(perk);
    }

    public static double multiplier(Perk perk) {
        return current().multiplier(perk);
    }

    public static double factor(Perk perk) {
        return current().factor(perk);
    }

    public static float spellDamage(double baseDamage) {
        return (float) (baseDamage * multiplier(Perk.ARCANE_CATALYST));
    }

    public static int experienceLevelCost(int baseLevels) {
        return discountedLevels(baseLevels, bonus(Perk.ASTRAL_NEGOTIATOR));
    }

    static int discountedLevels(int baseLevels, double percentOff) {
        if (baseLevels <= 0 || percentOff <= 0) return baseLevels;
        return (int) Math.round(baseLevels * (1D - percentOff / 100D));
    }

    public static int boostedCount(Perk perk, int baseCount) {
        return scaleCount(baseCount, multiplier(perk), ThreadLocalRandom.current().nextDouble());
    }

    static int scaleCount(int baseCount, double multiplier, double roll) {
        double scaled = baseCount * multiplier;
        int whole = (int) Math.floor(scaled);
        return roll < scaled - whole ? whole + 1 : whole;
    }
}
