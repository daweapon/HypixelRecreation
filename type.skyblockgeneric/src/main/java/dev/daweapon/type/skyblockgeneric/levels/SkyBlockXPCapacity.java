package dev.daweapon.type.skyblockgeneric.levels;

import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryExperience;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.skyblockgeneric.essence.EssenceShop;
import dev.daweapon.type.skyblockgeneric.essence.EssenceShops;
import dev.daweapon.type.skyblockgeneric.fishing.trophy.TrophyFishAwards;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeLevelAwards;
import dev.daweapon.type.skyblockgeneric.minion.MinionAwards;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerProgression;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

/**
 * How much of each SkyBlock XP task this server's content can actually award. A task is fully built unless listed
 * here or not in the game at all; the gap up to Hypixel's maximum is what {@code /max profile} credits as unbuilt
 * content.
 */
public final class SkyBlockXPCapacity {
    private static final Map<SkyBlockXPSource, DoubleSupplier> PARTIAL = new EnumMap<>(SkyBlockXPSource.class);

    static {
        PARTIAL.put(SkyBlockXPSource.BESTIARY_PROGRESS, BestiaryExperience::maxSkyBlockXp);
        PARTIAL.put(SkyBlockXPSource.CRAFT_MINIONS, MinionAwards::maxSkyBlockXp);
        PARTIAL.put(SkyBlockXPSource.HEART_OF_THE_MOUNTAIN, HotmService::maxSkyBlockXp);
        PARTIAL.put(SkyBlockXPSource.HEART_OF_THE_FOREST, HotfService::maxSkyBlockXp);
        PARTIAL.put(SkyBlockXPSource.SLAYER_LEVEL_UP, SlayerProgression::maxLevelSkyBlockXp);
        PARTIAL.put(SkyBlockXPSource.DEFEAT_SLAYERS, SlayerProgression::maxDefeatSkyBlockXp);
        PARTIAL.put(SkyBlockXPSource.CHAPTERS, ChapterService::maxSkyBlockXp);
        PARTIAL.put(SkyBlockXPSource.ATTRIBUTE_LEVELS, AttributeLevelAwards::maxSkyBlockXp);
        PARTIAL.put(SkyBlockXPSource.TROPHY_FISH, TrophyFishAwards::maxSkyBlockXp);
    }

    private SkyBlockXPCapacity() {
    }

    /** The most XP this server can award for {@code source}, never above Hypixel's maximum. */
    public static double built(SkyBlockXPSource source) {
        if (source.isUncapped()) return 0;
        double built = builtUnclamped(source);
        return Math.max(0, Math.min(source.getMaxXP(), built));
    }

    /** Hypixel's maximum minus what this server can award. */
    public static double unbuilt(SkyBlockXPSource source) {
        if (source.isUncapped()) return 0;
        return source.getMaxXP() - built(source);
    }

    public static Map<SkyBlockXPSource, Double> unbuiltCredit() {
        Map<SkyBlockXPSource, Double> credit = new EnumMap<>(SkyBlockXPSource.class);
        for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
            double unbuilt = unbuilt(source);
            if (unbuilt > 0) credit.put(source, unbuilt);
        }
        return credit;
    }

    private static double builtUnclamped(SkyBlockXPSource source) {
        DoubleSupplier partial = PARTIAL.get(source);
        if (partial != null) return partial.getAsDouble();
        for (EssenceShop<?> shop : EssenceShops.allWithXp()) {
            if (shop.xpSource() == source) return shop.availableSkyBlockXP();
        }
        return source.getAvailability() == SkyBlockXPSource.Availability.LIVE ? source.getMaxXP() : 0;
    }
}
