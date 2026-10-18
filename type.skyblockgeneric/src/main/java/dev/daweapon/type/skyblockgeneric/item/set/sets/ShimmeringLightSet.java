package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.type.skyblockgeneric.item.set.impl.TieredArmorSet;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
                "<a>" + (int) bonusPercent(wornPieces) + "%<7>.");
    }

    public static double bonusPercent(int wornPieces) {
        return wornPieces < 2 ? 0 : EXPERIENCE_ORB_BONUS[Math.min(wornPieces, 4) - 2];
    }

    public static double experienceOrbMultiplier(int wornPieces) {
        return 1 + bonusPercent(wornPieces) / 100;
    }

    public static double experienceOrbMultiplier(SkyBlockPlayer player) {
        return experienceOrbMultiplier(new ShimmeringLightSet().getWornPieceCount(player));
    }
}
