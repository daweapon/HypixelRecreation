package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityMath;
import dev.daweapon.type.skyblockgeneric.item.set.impl.TieredArmorSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class SnorkelingArmorSet implements TieredArmorSet {
    @Override
    public String getName() {
        return "Long Tuba";
    }

    @Override
    public int minimumPieces() {
        return 2;
    }

    @Override
    public List<String> getDescription(int wornPieces) {
        return List.of("<7>Grants <3>+" + (long) respiration(wornPieces) + " Respiration<7>.");
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, int wornPieces) {
        return ItemStatistics.builder().withBase(ItemStatistic.RESPIRATION, respiration(wornPieces)).build();
    }

    public static double respiration(int wornPieces) {
        return wornPieces < 2 ? 0 : ArmorAbilityMath.LONG_TUBA_RESPIRATION[Math.min(wornPieces, 4) - 2];
    }
}
