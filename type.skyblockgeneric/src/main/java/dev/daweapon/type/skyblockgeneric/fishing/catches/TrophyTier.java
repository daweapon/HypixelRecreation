package dev.daweapon.type.skyblockgeneric.fishing.catches;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.Optional;

/**
 * Trophy fish tier. Hypixel's item ids are {@code <FISH>_<TIER>} and the tier label is drawn in
 * dark gray, gray, gold or aqua.
 */
public enum TrophyTier {
    BRONZE(NamedTextColor.DARK_GRAY, "8", "Bronze", 4),
    SILVER(NamedTextColor.GRAY, "7", "Silver", 8),
    GOLD(NamedTextColor.GOLD, "6", "Gold", 16),
    DIAMOND(NamedTextColor.AQUA, "b", "Diamond", 32);

    private final TextColor colour;
    private final String colourCode;
    private final String displayName;
    private final int firstCatchSkyBlockXp;

    TrophyTier(TextColor colour, String colourCode, String displayName, int firstCatchSkyBlockXp) {
        this.colour = colour;
        this.colourCode = colourCode;
        this.displayName = displayName;
        this.firstCatchSkyBlockXp = firstCatchSkyBlockXp;
    }

    public TextColor colour() {
        return colour;
    }

    public String displayName() {
        return displayName;
    }

    public int firstCatchSkyBlockXp() {
        return firstCatchSkyBlockXp;
    }

    public String label() {
        return "<" + colourCode + "><l>" + name() + "</l>";
    }

    public String suffix() {
        return "_" + name();
    }

    public String itemId(String stem) {
        return stem + suffix();
    }

    public static Optional<TrophyTier> fromItemId(String itemId) {
        if (itemId == null) return Optional.empty();
        for (TrophyTier tier : values()) {
            if (itemId.endsWith(tier.suffix())) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }

    public static List<TrophyTier> orderedDescending() {
        return List.of(DIAMOND, GOLD, SILVER, BRONZE);
    }
}
