package dev.daweapon.type.skyblockgeneric.crystalhollows;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class CrystalHollowsZoneTips {
    private static final Map<RegionType, List<String>> TIPS = new EnumMap<>(RegionType.class);

    static {
        tips(RegionType.JUNGLE, "Fight Sludges.", "Mine Amethyst Gemstones.", "Meet the local tribe!");
        tips(RegionType.GOBLIN_HOLDOUT, "Home of Goblins.", "Rich with Amber Gemstones.");
        tips(RegionType.PRECURSOR_REMNANTS, "Mine Sapphire Gemstones.", "Watch out for Automatons!");
        tips(RegionType.MITHRIL_DEPOSITS, "Mine Mithril and Jade Gemstones.", "Meet the Keepers.");
        tips(RegionType.MAGMA_FIELDS, "Find Topaz Gemstones.", "Watch the temperature!");
        tips(RegionType.FAIRY_GROTTO, "Catch Butterflies.", "Mine Jasper Gemstones.", "Fish for a Fairy Soul.");
        tips(RegionType.CRYSTAL_NUCLEUS, "Place all Crystals at the statues.");
    }

    private CrystalHollowsZoneTips() {
    }

    private static void tips(RegionType zone, String... lines) {
        TIPS.put(zone, List.of(lines));
    }

    public static @Nullable Text[] of(RegionType zone) {
        List<String> lines = TIPS.get(zone);
        return lines == null ? null : lines.stream().map(Text::of).toArray(Text[]::new);
    }
}
