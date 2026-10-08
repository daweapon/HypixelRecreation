package net.swofty.type.skyblockgeneric.accessory.handler;

import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.accessory.AccessoryOwnership;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Keeps Night Vision topped up while the charm is owned. The effect is short and refreshed, so it
 * fades on its own once the charm is gone, and a permanent Night Vision from elsewhere is left alone.
 */
public final class NightVisionCharm {
    public static final int REFRESH_TICKS = 40;
    private static final int DURATION_TICKS = 400;

    private NightVisionCharm() {
    }

    public static void refresh(SkyBlockPlayer player) {
        if (!AccessoryOwnership.owns(player, ItemType.NIGHT_VISION_CHARM)) return;
        for (TimedPotion active : player.getActiveEffects()) {
            if (active.potion().effect() == PotionEffect.NIGHT_VISION && active.potion().duration() < 0) return;
        }
        player.addEffect(new Potion(PotionEffect.NIGHT_VISION, (byte) 0, DURATION_TICKS));
    }
}
