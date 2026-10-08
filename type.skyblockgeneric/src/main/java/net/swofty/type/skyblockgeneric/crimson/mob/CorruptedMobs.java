package net.swofty.type.skyblockgeneric.crimson.mob;

import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;

import java.util.List;

/**
 * Corrupted land mobs (wiki: Corrupted_Mobs): every Crimson Isle mob that can be Corrupted rolls 1% when it
 * spawns, 5% while the volcano erupts. They no longer get extra stats, and drop a Corrupted Fragment and
 * Sulphur. The Totem of Corruption, Corruption and Infection only affect Sea Creatures.
 */
public final class CorruptedMobs {
    static final double BASE_CHANCE = 1;
    static final double ERUPTION_CHANCE = 5;
    static final List<MobDrop> DROPS = List.of(MobDrop.always("CORRUPTED_FRAGMENT", 1), MobDrop.always("SULPHUR", 1));

    private CorruptedMobs() {
    }

    public static double chance() {
        return VolcanoEruptionService.isErupting() ? ERUPTION_CHANCE : BASE_CHANCE;
    }

    public static boolean rollOnSpawn(CrimsonMobProfile profile) {
        return profile.corruptible() && LootRoll.roll(null, null, chance());
    }
}
