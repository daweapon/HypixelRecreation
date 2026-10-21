package dev.daweapon.type.skyblockgeneric.moonglade;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class GalateaZoneTips {
    private static final Map<RegionType, List<String>> TIPS = new EnumMap<>(RegionType.class);

    static {
        tips(RegionType.TANGLEBURGS_PATH, "Talk to <b>Hina <f>about Heart of the Forest!",
                "Talk to <b>Swoop <f>about Tree Cutting",
                "Talk to <b>David Hunterborough <f>about Hunting!",
                "Compete in <d>Starlyn Contests<f>!");
        tips(RegionType.ANCIENT_RUINS, "Visit the ruins of an ancient temple.");
        tips(RegionType.FOREST_TEMPLE, "The Altar of an ancient being.");
        tips(RegionType.MURKWATER_DEPTHS, "Discover the lake and its secrets!", "Hunt all living creatures!");
    }

    private GalateaZoneTips() {
    }

    private static void tips(RegionType zone, String... lines) {
        TIPS.put(zone, List.of(lines));
    }

    public static @Nullable Text[] of(RegionType zone) {
        List<String> lines = TIPS.get(zone);
        return lines == null ? null : lines.stream().map(Text::of).toArray(Text[]::new);
    }
}
