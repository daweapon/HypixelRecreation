package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMob;

public class MobJockeySkeleton extends SpiderDenMob {
    private static final int SHOT_INTERVAL_TICKS = 60;
    private static final double SHOT_RANGE = 10;

    public MobJockeySkeleton(CrimsonMobProfile profile) {
        super(profile);
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % SHOT_INTERVAL_TICKS != 0) return;
        rangedTarget(SHOT_RANGE).ifPresent(target -> MobProjectile.item(this, target, ItemStack.of(Material.SILVERFISH_SPAWN_EGG), 0.9,
                hit -> strike(hit, getProfile().damage())));
    }
}
