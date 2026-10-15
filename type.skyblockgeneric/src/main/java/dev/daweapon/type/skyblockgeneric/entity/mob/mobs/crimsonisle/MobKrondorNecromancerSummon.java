package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;

/**
 * Krondor Necromancer Summon (wiki: Krondor_Necromancer_Summon): fast melee undead with 500 Defense and no drops.
 */
public class MobKrondorNecromancerSummon extends CrimsonIsleMob {
    public MobKrondorNecromancerSummon() {
        super(CrimsonMobs.KRONDOR_NECROMANCER_SUMMON);
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }
}
