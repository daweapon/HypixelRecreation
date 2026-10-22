package dev.daweapon.type.skyblockgeneric.moonglade;

import dev.daweapon.commons.skyblock.item.ItemType;

import java.util.Set;

public final class CoralFishFamily {
    public static final String FISH_SUFFIX = "_THE_FISH";
    public static final String SINGING_FISH = "SINGING_FISH";
    public static final String SOULBOUND_KEY = "coral.soulbound";
    public static final String TRIGGER = "coral.fish";

    private static final Set<String> REFUSED = Set.of(
            "CHILL_THE_FISH", "CHILL_THE_FISH_2", "EGG_THE_FISH", "OOPS_THE_FISH", "SPOOK_THE_FISH", SINGING_FISH);

    private CoralFishFamily() {
    }

    public static boolean accepts(ItemType type) {
        return type != null && accepts(type.name());
    }

    public static boolean accepts(String id) {
        return id.endsWith(FISH_SUFFIX) && !REFUSED.contains(id);
    }

    public static String shownKey(String id) {
        return "coral.fish." + id;
    }

    public static long fishingXp(int shownCount) {
        if (shownCount < 1) return 0;
        if (shownCount <= 5) return 10_000;
        if (shownCount <= 10) return 15_000;
        if (shownCount <= 21) return 20_000;
        if (shownCount <= 29) return 30_000;
        if (shownCount <= 34) return 40_000;
        return 0;
    }
}
