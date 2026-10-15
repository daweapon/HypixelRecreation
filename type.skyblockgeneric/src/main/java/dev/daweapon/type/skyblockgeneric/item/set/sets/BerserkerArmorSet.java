package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.set.impl.TieredArmorSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class BerserkerArmorSet implements TieredArmorSet {
    private static final int[] HEALTH_COST_REDUCTION = {20, 40, 60};

    public static double healthCostMultiplier(SkyBlockPlayer player) {
        BerserkerArmorSet set = new BerserkerArmorSet();
        int worn = set.getWornPieceCount(player);
        if (worn < set.minimumPieces()) return 1;
        return 1 - HEALTH_COST_REDUCTION[TieredArmorSet.tierIndex(worn, set.minimumPieces())] / 100.0;
    }

    @Override
    public String getName() {
        return "Berserk";
    }

    @Override
    public int minimumPieces() {
        return 2;
    }

    @Override
    public List<String> getDescription(int wornPieces) {
        int reduction = HEALTH_COST_REDUCTION[TieredArmorSet.tierIndex(wornPieces, minimumPieces())];
        return List.of(
                "<7>Reduces your health and defense by",
                "<7>half and reduces the cost of",
                "<7>health-consuming abilities by <c>" + reduction + "%<7>.");
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, int wornPieces) {
        return ItemStatistics.builder()
                .withMultiplicative(ItemStatistic.HEALTH, 0.5)
                .withMultiplicative(ItemStatistic.DEFENSE, 0.5)
                .build();
    }
}
