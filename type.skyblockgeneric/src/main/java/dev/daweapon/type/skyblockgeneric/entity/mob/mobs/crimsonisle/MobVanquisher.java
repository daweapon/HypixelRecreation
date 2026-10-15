package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Vanquisher (wiki: Vanquisher): a small Wither that can rise from any Crimson Isle kill. It shrugs off damage
 * for its first 3 seconds (the wiki gives no length, so this is an estimate).
 */
public class MobVanquisher extends CrimsonIsleMob {
    public MobVanquisher() {
        super(CrimsonMobs.VANQUISHER);
    }

    private static final int SPAWN_RESISTANCE_TICKS = 60;

    @Override
    protected float aggroRange() {
        return 24;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        getAttribute(Attribute.SCALE).setBaseValue(0.6);
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        return getAliveTicks() >= SPAWN_RESISTANCE_TICKS;
    }
}
