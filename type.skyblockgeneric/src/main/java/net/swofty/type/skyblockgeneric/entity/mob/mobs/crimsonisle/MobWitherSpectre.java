package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Wither Spectre (wiki: Wither_Spectre): runs straight at nearby players and withers them. Shown as a floating
 * skull over an invisible body (estimate; the wiki has only a GIF).
 */
public class MobWitherSpectre extends CrimsonIsleMob {
    public MobWitherSpectre() {
        super(CrimsonMobs.WITHER_SPECTRE);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setInvisible(true);
        setHelmet(ItemStack.of(Material.WITHER_SKELETON_SKULL));
    }

    @Override
    public void onMeleeHit(SkyBlockPlayer target) {
        target.addEffect(new Potion(PotionEffect.WITHER, (byte) 0, 200));
    }
}
