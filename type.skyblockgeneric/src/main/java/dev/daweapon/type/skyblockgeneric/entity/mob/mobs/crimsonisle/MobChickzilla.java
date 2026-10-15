package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Chickzilla (wiki: Chickzilla): Igrupan's giant chicken, spawned by his quest. It only takes melee damage and
 * leaves if it kills the player.
 */
public class MobChickzilla extends CrimsonIsleMob {
    public MobChickzilla() {
        super(CrimsonMobs.CHICKZILLA);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        getAttribute(Attribute.SCALE).setBaseValue(4);
    }

    @Override
    public boolean isImmuneToArrows() {
        return true;
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        return damage.getType() == DamageType.PLAYER_ATTACK && player.getPosition().distance(getPosition()) <= 8;
    }

    @Override
    public void onMeleeHit(SkyBlockPlayer target) {
        if (target.getHealth() <= 0) remove();
    }
}
