package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import net.minestom.server.entity.damage.Damage;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public interface SlayerBossAbility {
    void onSpawn(SlayerBossMob boss);

    float modifyIncomingDamage(SlayerBossMob boss, Damage damage, float amount);

    void onDamaged(SlayerBossMob boss, Damage damage, float appliedDamage);

    void onMeleeHit(SlayerBossMob boss, SkyBlockPlayer target);

    void onDeath(SlayerBossMob boss);

    default boolean canAttack(SlayerBossMob boss) {
        return true;
    }

    default String nameHeader(SlayerBossMob boss) {
        return "";
    }

    static SlayerBossAbility none() {
        return NoopSlayerBossAbility.INSTANCE;
    }
}
