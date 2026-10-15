package dev.daweapon.type.skyblockgeneric.fishing.trophy;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Trophy fish items were first saved as {@code <TIER>_<FISH>} (e.g. {@code BRONZE_LAVAHORSE}); Hypixel's ids
 * are {@code <FISH>_<TIER>} (e.g. {@code LAVA_HORSE_BRONZE}). Rewrites an old saved id to its Hypixel id.
 */
public final class TrophyFishLegacyIds {
    private static final Pattern LEGACY = Pattern.compile(
            "^(BRONZE|SILVER|GOLD|DIAMOND)_(SULPHUR_SKITTER|BLOBFISH|OBFUSCATED_[123]|STEAMING_HOT_FLOUNDER|GUSHER|SLUGFISH"
                    + "|FLYFISH|VANILLE|LAVAHORSE|MANA_RAY|VOLCANIC_STONEFISH|SKELETON_FISH|MOLDFIN|SOUL_FISH|KARATE_FISH|GOLDEN_FISH)$");
    private static final Map<String, String> RENAMED_STEMS = Map.of(
            "LAVAHORSE", "LAVA_HORSE",
            "OBFUSCATED_1", "OBFUSCATED_FISH_1",
            "OBFUSCATED_2", "OBFUSCATED_FISH_2",
            "OBFUSCATED_3", "OBFUSCATED_FISH_3");

    private TrophyFishLegacyIds() {
    }

    public static @Nullable String migrate(@Nullable String savedId) {
        if (savedId == null) return null;
        Matcher matcher = LEGACY.matcher(savedId);
        if (!matcher.matches()) return savedId;
        String stem = RENAMED_STEMS.getOrDefault(matcher.group(2), matcher.group(2));
        return stem + "_" + matcher.group(1);
    }
}
