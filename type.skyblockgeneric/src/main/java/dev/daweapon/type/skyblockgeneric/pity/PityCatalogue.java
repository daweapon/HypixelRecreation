package dev.daweapon.type.skyblockgeneric.pity;

import java.util.List;

public final class PityCatalogue {
    private PityCatalogue() {
    }

    public static List<PityCategory> categories() {
        return PityCatalogueData.ALL;
    }
}
