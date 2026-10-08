package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Goliath Barbarian (wiki: Goliath_Barbarian): a rare Barbarian that, once attacked, raises its max health every
 * 10 s and refills to it: 5M, 7.5M, 8.7M, 10M, then 11M. Killing one costs 25 Barbarian Reputation.
 */
public class MobGoliathBarbarian extends CrimsonIsleMob {
    public MobGoliathBarbarian() {
        super(CrimsonMobs.GOLIATH_BARBARIAN);
    }

    private static final double[] GROWTH = {7_500_000, 8_700_000, 10_000_000, 11_000_000};
    private static final int GROWTH_INTERVAL_TICKS = 200;
    private int stage = -1;

    @Override
    protected Temper temper() {
        return Temper.NEUTRAL;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        getAttribute(Attribute.SCALE).setBaseValue(1.5);
        setItemInMainHand(ItemStack.of(Material.GOLDEN_AXE));
    }

    @Override
    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
        if (stage < 0) stage = 0;
    }

    @Override
    protected void onAiTick(int ticks) {
        if (stage < 0 || stage >= GROWTH.length || ticks % GROWTH_INTERVAL_TICKS != 0) return;
        setMaxHealth((float) GROWTH[stage++]);
        refreshName();
    }
}
