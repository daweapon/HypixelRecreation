package dev.daweapon.type.skyblockgeneric.minion.inferno;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;

/**
 * The unique items Hypergolic fuel lets an Inferno Minion create, each rolled once per harvest
 * (wiki, Inferno Minion Fuel). Capsaicin Eyedrops raise every one except the Chili Pepper by 30%.
 */
@Getter
public enum InfernoRareDrop {
    CHILI_PEPPER(ItemType.CHILI_PEPPER, 136, false),
    INFERNO_VERTEX(ItemType.INFERNO_VERTEX, 5_950, true),
    INFERNO_APEX(ItemType.INFERNO_APEX, 1_309_091, true),
    REAPER_PEPPER(ItemType.REAPER_PEPPER, 458_182, true),
    GABAGOOL_THE_FISH(ItemType.GABAGOOL_THE_FISH, 3_927_273, true);

    public static final double EYEDROPS_BOOST = 1.3;
    private static final int APEX_MINION_TIER = 10;

    private final ItemType item;
    private final int oneIn;
    private final boolean boostedByEyedrops;

    InfernoRareDrop(ItemType item, int oneIn, boolean boostedByEyedrops) {
        this.item = item;
        this.oneIn = oneIn;
        this.boostedByEyedrops = boostedByEyedrops;
    }

    public double chancePercent(boolean eyedrops) {
        double chance = 100.0 / oneIn;
        return eyedrops && boostedByEyedrops ? chance * EYEDROPS_BOOST : chance;
    }

    public int amount(int minionTier) {
        return this == INFERNO_APEX && minionTier >= APEX_MINION_TIER ? 2 : 1;
    }
}
