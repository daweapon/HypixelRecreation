package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.type.skyblockgeneric.item.set.impl.TieredArmorSet;

import java.util.List;

public class ShimmeringLightSet implements TieredArmorSet {
    private static final int[] EXPERIENCE_ORB_BONUS = {200, 200, 300};

    @Override
    public String getName() {
        return "Shimmer";
    }

    @Override
    public int minimumPieces() {
        return 2;
    }

    @Override
    public List<String> getDescription(int wornPieces) {
        return List.of(
                "<7>Increases all <a>Experience Orbs",
                "<7>gained from monsters and ores by",
                "<a>" + EXPERIENCE_ORB_BONUS[TieredArmorSet.tierIndex(wornPieces, minimumPieces())] + "%<7>.");
    }
}
