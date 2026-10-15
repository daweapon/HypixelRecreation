package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;

/**
 * Barbarian (wiki: Barbarian): neutral until provoked. Killing one costs 5 Barbarian Reputation.
 */
public class MobBarbarian extends CrimsonIsleMob {
    public MobBarbarian() {
        super(CrimsonMobs.BARBARIAN);
    }

    @Override
    protected Temper temper() {
        return Temper.NEUTRAL;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setItemInMainHand(ItemStack.of(Material.IRON_SWORD));
    }
}
