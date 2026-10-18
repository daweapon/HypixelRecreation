package dev.daweapon.type.skyblockgeneric.minion;

import java.util.Set;

public final class MinionCategories {
    private static final Set<String> FARMING = Set.of(
            "WHEAT", "POTATO", "CACTUS", "CARROT", "COCOA_BEANS", "MELON", "MUSHROOM", "NETHER_WART", "PUMPKIN",
            "SUGAR_CANE", "CHICKEN", "COW", "PIG", "RABBIT", "SHEEP", "SUNFLOWER");
    private static final Set<String> FORAGING = Set.of(
            "ACACIA", "BIRCH", "DARK_OAK", "JUNGLE", "OAK", "SPRUCE", "FLOWER");
    private static final Set<String> WINTER = Set.of("SNOW", "ICE");

    private MinionCategories() {
    }

    public static boolean isFarming(String minionName) {
        return FARMING.contains(minionName);
    }

    public static boolean isForaging(String minionName) {
        return FORAGING.contains(minionName);
    }

    public static boolean isWinter(String minionName) {
        return WINTER.contains(minionName);
    }

    public static boolean isFarming(MinionRegistry minion) {
        return isFarming(minion.name());
    }
}
