package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.entity.damage.Damage;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Ashfang Underling (wiki: Ashfang_Underling): an Acolyte that only ranged and magic attacks can hurt.
 */
public class MobAshfangUnderling extends MobAshfangAcolyte {
    public MobAshfangUnderling() {
        super(CrimsonMobs.ASHFANG_UNDERLING);
    }

    @Override
    protected boolean hurtBy(SkyBlockPlayer player, Damage damage) {
        return !super.hurtBy(player, damage);
    }
}
