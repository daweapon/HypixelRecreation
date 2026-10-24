package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMob;

public class MobWeaverSpider extends SpiderDenMob {
    private static final int WEB_INTERVAL_TICKS = 80;
    private static final double WEB_RANGE = 8;
    private static final int SLOW_TICKS = 100;
    private static final int MAX_SLOW = 4;

    public MobWeaverSpider(CrimsonMobProfile profile) {
        super(profile);
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % WEB_INTERVAL_TICKS != 0) return;
        rangedTarget(WEB_RANGE).ifPresent(target -> MobProjectile.item(this, target, ItemStack.of(Material.COBWEB), 0.9, hit -> {
            int level = hit.getActiveEffects().stream()
                    .filter(active -> active.potion().effect() == PotionEffect.SLOWNESS)
                    .mapToInt(active -> active.potion().amplifier() + 1).max().orElse(0);
            hit.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) Math.min(MAX_SLOW, level), SLOW_TICKS));
        }));
    }
}
