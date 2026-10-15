package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;

/**
 * Kada Knight (wiki: Kada_Knight): a small knight in the caves around the Magma Chamber, shown as a baby zombie.
 */
public class MobKadaKnight extends CrimsonIsleMob {
    public MobKadaKnight() {
        super(CrimsonMobs.KADA_KNIGHT);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(ZombieMeta.class, meta -> meta.setBaby(true));
        setItemInMainHand(ItemStack.of(Material.IRON_SWORD));
    }
}
