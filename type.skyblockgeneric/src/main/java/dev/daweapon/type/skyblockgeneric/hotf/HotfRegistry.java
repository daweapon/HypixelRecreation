package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HotfRegistry {
    public static final Path DIRECTORY = Path.of("./configuration/skyblock/hotf");
    private static volatile HotfRegistry loaded;

    private final Map<String, HotfPerk> perks;
    private final List<HotfCoreLevel> coreLevels;
    private final List<HotfTierReward> tiers;
    private final boolean desertWhispers;

    private HotfRegistry(Map<String, HotfPerk> perks, List<HotfCoreLevel> coreLevels, List<HotfTierReward> tiers,
                         boolean desertWhispers) {
        this.perks = perks;
        this.coreLevels = coreLevels;
        this.tiers = tiers;
        this.desertWhispers = desertWhispers;
    }

    public static HotfRegistry get() {
        HotfRegistry current = loaded;
        if (current != null) return current;
        synchronized (HotfRegistry.class) {
            if (loaded == null) loaded = load(DIRECTORY);
            return loaded;
        }
    }

    public static HotfRegistry load(Path directory) {
        Map<String, Object> perkRoot = read(directory.resolve("perks.yml"));
        Map<String, Object> coreRoot = read(directory.resolve("core.yml"));
        boolean desert = Boolean.TRUE.equals(perkRoot.get("desertWhispers"));
        int defaultTokens = number(perkRoot.get("tokenCostPerPerk"), 1).intValue();

        Map<String, HotfPerk> perks = new LinkedHashMap<>();
        for (Map<String, Object> entry : maps(perkRoot.get("perks"))) {
            HotfPerk perk = parsePerk(entry, defaultTokens, desert);
            perks.put(perk.id(), perk);
        }

        List<HotfCoreLevel> core = new ArrayList<>();
        for (Map<String, Object> entry : maps(coreRoot.get("levels"))) {
            HotfCurrency currency = HotfCurrency.valueOf((String) entry.get("currency"));
            core.add(new HotfCoreLevel(
                    number(entry.get("level"), 0).intValue(),
                    desert ? currency : HotfCurrency.FOREST,
                    number(entry.get("cost"), 0).longValue(),
                    number(entry.get("tokens"), 0).intValue(),
                    number(entry.get("skyBlockXp"), 0).intValue(),
                    (String) entry.get("reward")));
        }

        List<HotfTierReward> tiers = new ArrayList<>();
        for (Map<String, Object> entry : maps(coreRoot.get("tiers"))) {
            tiers.add(new HotfTierReward(
                    number(entry.get("tier"), 0).intValue(),
                    number(entry.get("experience"), 0).longValue(),
                    number(entry.get("tokens"), 0).intValue(),
                    number(entry.get("skyBlockXp"), 0).intValue(),
                    strings(entry.get("rewards"))));
        }
        return new HotfRegistry(Map.copyOf(perks), List.copyOf(core), List.copyOf(tiers), desert);
    }

    private static HotfPerk parsePerk(Map<String, Object> entry, int defaultTokens, boolean desert) {
        String currencyName = (String) entry.get("currency");
        HotfCurrency currency = currencyName == null ? null : HotfCurrency.valueOf(currencyName);
        if (currency == HotfCurrency.DESERT && !desert) currency = HotfCurrency.FOREST;
        List<HotfPerk.Effect> effects = new ArrayList<>();
        for (Map<String, Object> effect : maps(entry.get("effects"))) {
            String stat = (String) effect.get("stat");
            Object cap = effect.get("cap");
            effects.add(new HotfPerk.Effect(
                    (String) effect.get("key"),
                    stat == null ? null : ItemStatistic.valueOf(stat),
                    number(effect.get("base"), 0).doubleValue(),
                    number(effect.get("perLevel"), 0).doubleValue(),
                    cap == null ? null : number(cap, 0).doubleValue(),
                    HotfScope.valueOf((String) effect.get("scope"))));
        }
        return new HotfPerk(
                (String) entry.get("id"),
                (String) entry.get("name"),
                HotfPerk.Kind.valueOf((String) entry.getOrDefault("kind", "PERK")),
                currency,
                number(entry.get("tier"), 1).intValue(),
                number(entry.get("col"), 4).intValue(),
                number(entry.get("maxLevel"), 1).intValue(),
                number(entry.get("exponent"), 0).doubleValue(),
                number(entry.get("offset"), 0).intValue(),
                number(entry.get("linearBase"), 0).intValue(),
                number(entry.get("linearSlope"), 0).intValue(),
                !Boolean.FALSE.equals(entry.get("available")),
                (String) entry.get("unavailable"),
                (String) entry.get("exclusive"),
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

    public Map<String, HotfPerk> perks() {
        return perks;
    }

    public HotfPerk perk(String id) {
        return perks.get(id);
    }

    public List<HotfCoreLevel> coreLevels() {
        return coreLevels;
    }

    public HotfCoreLevel coreLevel(int level) {
        return level >= 1 && level <= coreLevels.size() ? coreLevels.get(level - 1) : null;
    }

    public int maxCoreLevel() {
        return coreLevels.size();
    }

    public List<HotfTierReward> tiers() {
        return tiers;
    }

    public int maxTier() {
        return tiers.size();
    }

    public HotfTierReward tierReward(int tier) {
        return tiers.get(Math.min(Math.max(tier, 1), tiers.size()) - 1);
    }

    public int tierOf(long experience) {
        int tier = 1;
        for (HotfTierReward reward : tiers) {
            if (experience >= reward.experience()) tier = reward.tier();
        }
        return tier;
    }

    public long maxExperience() {
        return tiers.getLast().experience();
    }

    public boolean desertWhispersEnabled() {
        return desertWhispers;
    }
}
