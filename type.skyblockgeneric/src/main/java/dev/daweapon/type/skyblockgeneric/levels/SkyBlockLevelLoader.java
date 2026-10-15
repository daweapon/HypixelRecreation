package dev.daweapon.type.skyblockgeneric.levels;

import lombok.Data;
import net.kyori.adventure.key.InvalidKeyException;
import net.minestom.server.item.Material;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.levels.unlocks.CustomLevelUnlock;
import dev.daweapon.type.skyblockgeneric.levels.unlocks.SkyBlockLevelStatisticUnlock;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SkyBlockLevelLoader {
    private static final String FILE_NAME = "skyblock_level_rewards";
    private static final File LEVELS_DIR = new File("./configuration/skyblock/levels");

    @Data
    public static class LevelConfig {
        public List<LevelEntry> levels;
    }

    @Data
    public static class LevelEntry {
        public int level;
        public int experience;
        public boolean isMilestone;
        public String prefix;
        public @Nullable String prefixDisplay;
        public @Nullable String prefixItem;
        public List<LevelUnlock> unlocks;
    }

    @Data
    public static class LevelUnlock {
        public String type;
        public LevelUnlockData data;
    }

    @Data
    public static class LevelUnlockData {
        // Statistic unlock data
        public Map<String, StatisticValue> statistics;

        // Custom award data
        public String customAward;
    }

    @Data
    public static class StatisticValue {
        public @Nullable Double base;
        public @Nullable Double additive;
        public @Nullable Double additivePercentage;
        public @Nullable Double multiplicative;
        public @Nullable Double multiplicativePercentage;
    }

    public static SkyBlockLevelRequirement[] loadFromFile() {
        try {
            // Ensure directory exists
            if (!YamlFileUtils.ensureDirectoryExists(LEVELS_DIR)) {
                throw new IOException("Failed to create levels directory");
            }

            File levelFile = new File(LEVELS_DIR, FILE_NAME + ".yml");

            // Load the YAML file
            Yaml yaml = new Yaml();
            LevelConfig config = yaml.loadAs(new FileReader(levelFile), LevelConfig.class);

            List<SkyBlockLevelRequirement> requirements = new ArrayList<>();

            for (LevelEntry entry : config.levels) {
                SkyBlockLevelRequirement requirement = parseLevel(entry);
                requirements.add(requirement);
            }

            return requirements.toArray(new SkyBlockLevelRequirement[0]);
        } catch (Exception e) {
            Logger.error(e, "Failed to load SkyBlock level requirements from file: {}", FILE_NAME);
            return new SkyBlockLevelRequirement[0];
        }
    }

    private static SkyBlockLevelRequirement parseLevel(LevelEntry entry) {
        List<SkyBlockLevelUnlock> unlocks = new ArrayList<>();

        if (entry.unlocks != null) {
            for (LevelUnlock unlock : entry.unlocks) {
                switch (unlock.type.toUpperCase()) {
                    case "STATISTIC" -> unlocks.add(parseStatisticUnlock(unlock.data));
                    case "CUSTOM" -> unlocks.add(parseCustomUnlock(unlock.data));
                }
            }
        }

        Material prefixItem = parsePrefixItem(entry.prefixItem, entry.level);

        return createSkyBlockLevelRequirement(
                entry.level,
                entry.experience,
                entry.isMilestone,
                unlocks,
                entry.prefix != null ? entry.prefix : "<7>",
                entry.prefixDisplay,
                prefixItem
        );
    }

    private static SkyBlockLevelStatisticUnlock parseStatisticUnlock(LevelUnlockData data) {
        ItemStatistics.Builder builder = ItemStatistics.builder();

        if (data.statistics != null) {
            for (Map.Entry<String, StatisticValue> entry : data.statistics.entrySet()) {
                try {
                    ItemStatistic stat = ItemStatistic.valueOf(entry.getKey().toUpperCase());
                    StatisticValue value = entry.getValue();

                    if (value.base != null) {
                        builder.withBase(stat, value.base);
                    }
                    if (value.additive != null) {
                        builder.withAdditive(stat, value.additive);
                    }
                    if (value.additivePercentage != null) {
                        builder.withAdditivePercentage(stat, value.additivePercentage);
                    }
                    if (value.multiplicative != null) {
                        builder.withMultiplicative(stat, value.multiplicative);
                    }
                    if (value.multiplicativePercentage != null) {
                        builder.withMultiplicativePercentage(stat, value.multiplicativePercentage);
                    }
                } catch (IllegalArgumentException e) {
                    Logger.warn("Unknown statistic: {}", entry.getKey());
                }
            }
        }

        return new SkyBlockLevelStatisticUnlock(builder.build());
    }

    private static CustomLevelUnlock parseCustomUnlock(LevelUnlockData data) {
        try {
            CustomLevelAward award = CustomLevelAward.valueOf(data.customAward.toUpperCase());
            return new CustomLevelUnlock(award);
        } catch (IllegalArgumentException e) {
            Logger.error("Unknown custom award: {}", data.customAward);
            throw new RuntimeException("Unknown custom award: " + data.customAward);
        }
    }

    // Helper method to create SkyBlockLevelRequirement instances

    /**
     * A level's prefix reward is rendered as an item, so an unresolved material would reach the
     * GUI as null and throw while the menu was being built. Anything unrecognised falls back to a
     * visible placeholder and is logged instead.
     */
    private static Material parsePrefixItem(@Nullable String raw, int level) {
        if (raw == null || raw.isBlank()) return null;

        String name = raw.trim();
        int separator = Math.max(name.lastIndexOf('.'), name.lastIndexOf(':'));
        if (separator >= 0) name = name.substring(separator + 1);
        String key = name.toLowerCase();

        try {
            Material resolved = Material.fromKey(key);
            if (resolved != null) return resolved;
        } catch (IllegalArgumentException | InvalidKeyException ignored) {
            // Fall through to the slower lookup below.
        }

        Material resolved = Material.values().stream()
                .filter(material -> material.key().value().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);

        if (resolved == null) {
            Logger.warn("Level " + level + " has an unknown prefixItem '" + raw + "'; using a placeholder");
            return Material.GRAY_DYE;
        }
        return resolved;
    }

    private static SkyBlockLevelRequirement createSkyBlockLevelRequirement(
            int level,
            int experience,
            boolean isMilestone,
            List<SkyBlockLevelUnlock> unlocks,
            String prefix,
            String prefixDisplay,
            Material prefixItem) {

        return new SkyBlockLevelRequirement(
                level,
                experience,
                isMilestone,
                unlocks,
                prefix,
                prefixDisplay,
                prefixItem
        );
    }

    // Method to initialize the CustomLevelAward cache from loaded data
    public static void initializeCustomLevelAwardCache(SkyBlockLevelRequirement[] requirements) {
        for (SkyBlockLevelRequirement requirement : requirements) {
            List<CustomLevelUnlock> customUnlocks = requirement.getUnlocks().stream()
                    .filter(unlock -> unlock instanceof CustomLevelUnlock)
                    .map(unlock -> (CustomLevelUnlock) unlock)
                    .toList();

            for (CustomLevelUnlock unlock : customUnlocks) {
                CustomLevelAward.addToCache(requirement.asInt(), unlock.getAward());
            }
        }
    }
}