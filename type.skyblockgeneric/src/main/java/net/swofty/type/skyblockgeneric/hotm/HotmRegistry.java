package net.swofty.type.skyblockgeneric.hotm;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HotmRegistry {
    public static final Path DIRECTORY = Path.of("./configuration/skyblock/hotm");
    private static volatile HotmRegistry loaded;

    private final Map<String, HotmPerk> perks;
    private final List<HotmCoreLevel> coreLevels;
    private final List<HotmTierReward> tiers;

    private HotmRegistry(Map<String, HotmPerk> perks, List<HotmCoreLevel> coreLevels, List<HotmTierReward> tiers) {
        this.perks = perks;
        this.coreLevels = coreLevels;
        this.tiers = tiers;
    }

    public static HotmRegistry get() {
        HotmRegistry current = loaded;
        if (current != null) return current;
        synchronized (HotmRegistry.class) {
            if (loaded == null) loaded = load(DIRECTORY);
            return loaded;
        }
    }

    public static HotmRegistry load(Path directory) {
        Map<String, Object> perkRoot = read(directory.resolve("perks.yml"));
        Map<String, Object> coreRoot = read(directory.resolve("core.yml"));

        int defaultTokens = number(perkRoot.get("tokenCostPerPerk"), 1).intValue();
        Map<String, HotmPerk> perks = new LinkedHashMap<>();
        for (Map<String, Object> entry : maps(perkRoot.get("perks"))) {
            HotmPerk perk = parsePerk(entry, defaultTokens);
            perks.put(perk.id(), perk);
        }

        List<HotmCoreLevel> core = new ArrayList<>();
        for (Map<String, Object> entry : maps(coreRoot.get("levels"))) {
            core.add(new HotmCoreLevel(
                    number(entry.get("level"), 0).intValue(),
                    HotmPowder.valueOf((String) entry.get("powder")),
                    number(entry.get("cost"), 0).longValue(),
                    number(entry.get("tokens"), 0).intValue(),
                    number(entry.get("skyBlockXp"), 0).intValue(),
                    (String) entry.get("reward")));
        }

        List<HotmTierReward> tiers = new ArrayList<>();
        for (Map<String, Object> entry : maps(coreRoot.get("tiers"))) {
            tiers.add(new HotmTierReward(
                    number(entry.get("tier"), 0).intValue(),
                    number(entry.get("tokens"), 0).intValue(),
                    number(entry.get("forgeSlots"), 0).intValue(),
                    number(entry.get("skyBlockXp"), 0).intValue(),
                    strings(entry.get("rewards"))));
        }
        return new HotmRegistry(Map.copyOf(perks), List.copyOf(core), List.copyOf(tiers));
    }

    private static HotmPerk parsePerk(Map<String, Object> entry, int defaultTokens) {
        String powder = (String) entry.get("powder");
        List<HotmPerk.Effect> effects = new ArrayList<>();
        for (Map<String, Object> effect : maps(entry.get("effects"))) {
            String stat = (String) effect.get("stat");
            Object cap = effect.get("cap");
            effects.add(new HotmPerk.Effect(
                    (String) effect.get("key"),
                    stat == null ? null : ItemStatistic.valueOf(stat),
                    number(effect.get("base"), 0).doubleValue(),
                    number(effect.get("perLevel"), 0).doubleValue(),
                    cap == null ? null : number(cap, 0).doubleValue(),
                    HotmScope.valueOf((String) effect.get("scope"))));
        }
        String requires = (String) entry.get("requires");
        return new HotmPerk(
                (String) entry.get("id"),
                (String) entry.get("name"),
                HotmPerk.Kind.valueOf((String) entry.getOrDefault("kind", "PERK")),
                powder == null ? null : HotmPowder.valueOf(powder),
                number(entry.get("tier"), 1).intValue(),
                number(entry.get("col"), 4).intValue(),
                number(entry.get("maxLevel"), 1).intValue(),
                number(entry.get("exponent"), 0).doubleValue(),
                number(entry.get("offset"), 0).intValue(),
                !Boolean.FALSE.equals(entry.get("available")),
                requires,
                strings(entry.get("lore")),
                number(entry.get("tokens"), defaultTokens).intValue(),
                List.copyOf(effects));
    }

    @SuppressWarnings("unchecked")
    private static List<String> strings(Object value) {
        return value instanceof List<?> list ? List.copyOf((List<String>) list) : List.of();
    }

    private static Number number(Object value, Number fallback) {
        return value instanceof Number number ? number : fallback;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    private static Map<String, Object> read(Path file) {
        try (InputStream stream = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(stream);
            return root == null ? Map.of() : root;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + file, exception);
        }
    }

    public Map<String, HotmPerk> perks() {
        return perks;
    }

    public HotmPerk perk(String id) {
        return perks.get(id);
    }

    public List<HotmCoreLevel> coreLevels() {
        return coreLevels;
    }

    public HotmCoreLevel coreLevel(int level) {
        return level >= 1 && level <= coreLevels.size() ? coreLevels.get(level - 1) : null;
    }

    public int maxCoreLevel() {
        return coreLevels.size();
    }

    public HotmTierReward tierReward(int tier) {
        return tiers.get(Math.min(Math.max(tier, 1), tiers.size()) - 1);
    }
}
