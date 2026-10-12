package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.armor.ArmorAbilityMath;
import net.swofty.type.skyblockgeneric.item.set.impl.TieredArmorSet;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
