package net.swofty.type.skyblockgeneric.item.set.sets;

import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GolemArmorSet implements ArmorSet {
    public static final int ABSORPTION_AMPLIFIER = 2;
    public static final int ABSORPTION_TICKS = 20 * 20;

    public static void onKill(SkyBlockPlayer player) {
        if (!new GolemArmorSet().isWearingSet(player)) return;
        player.addEffect(new Potion(PotionEffect.ABSORPTION, (byte) ABSORPTION_AMPLIFIER, ABSORPTION_TICKS));
    }

    @Override
    public String getName() {
        return "Absorption";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "<7>Grants the wearer <a>Absorption 3<7> for",
                "<a>20 seconds<7> when they kill an enemy."));
    }
}
