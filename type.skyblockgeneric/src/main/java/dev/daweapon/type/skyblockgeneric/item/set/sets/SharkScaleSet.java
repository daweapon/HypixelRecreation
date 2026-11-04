package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class SharkScaleSet extends AbsorbSet {
    public static final int FESTIVAL_FISHER_MINIMUM_PIECES = 2;

    public static double festivalFisherBonus(int wornPieces) {
        if (wornPieces < FESTIVAL_FISHER_MINIMUM_PIECES) return 0;
        return 10 + (Math.min(wornPieces, 4) - FESTIVAL_FISHER_MINIMUM_PIECES) * 5;
    }

    public static double festivalFisherBonus(SkyBlockPlayer player) {
        return festivalFisherBonus(new SharkScaleSet().getWornPieceCount(player));
    }
}
