package dev.daweapon.type.skyblockgeneric.dante;

import dev.daweapon.type.skyblockgeneric.item.set.ArmorSetRegistry;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class ResistanceArmorSet implements ArmorSet {
    public static final double DAMAGE_BONUS = 1.25;
    public static final double DAMAGE_TAKEN = 0.5;

    public static boolean worn(SkyBlockPlayer player) {
        ArmorSetRegistry set = player.getArmorSet();
        return set == ArmorSetRegistry.RESISTANCE || set == ArmorSetRegistry.GENERALS_RESISTANCE;
    }

    public static double damageBonus(SkyBlockPlayer player) {
        return worn(player) ? DAMAGE_BONUS : 1;
    }

    public static double damageTakenMultiplier(SkyBlockPlayer player) {
        return worn(player) ? DAMAGE_TAKEN : 1;
    }

    @Override
    public String getName() {
        return "Power of the Resistance";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "Provides a damage bonus while",
                "fighting against Dante, and reduces",
                "damage taken from Dante."));
    }
}
