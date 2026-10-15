package dev.daweapon.type.skyblockgeneric.fishing.lava;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Hotspot Sea Creatures that set players alight (wiki: Fried_Chicken, Fireproof_Witch): the Fried Chicken ignites
 * whoever it hits; the Fireproof Witch throws splash Harming potions and ignites nearby players. Throw interval and
 * burn length are estimates.
 */
public class IgnitingSeaCreatureMob extends LavaSeaCreatureMob {
    static final int BURN_TICKS = 60;
    static final int POTION_INTERVAL_TICKS = 60;
    private static final double RANGE = 16;

    public IgnitingSeaCreatureMob(LavaSeaCreature creature, @Nullable SkyBlockPlayer angler, boolean corrupted) {
        super(creature, angler, corrupted);
    }

    @Override
    public void onMeleeHit(SkyBlockPlayer target) {
        super.onMeleeHit(target);
        target.setFireTicks(BURN_TICKS);
    }

    @Override
    protected void onTick(int ticks) {
        if (getCreature() != LavaSeaCreature.FIREPROOF_WITCH || ticks % POTION_INTERVAL_TICKS != 0) return;
        LavaAttacks.target(this, RANGE).ifPresent(target -> MobProjectile.item(this, target,
                ItemStack.of(Material.SPLASH_POTION), 0.7, hit -> {
                    hit(hit, getCreature().getDamage());
                    hit.setFireTicks(BURN_TICKS);
                }));
    }
}
