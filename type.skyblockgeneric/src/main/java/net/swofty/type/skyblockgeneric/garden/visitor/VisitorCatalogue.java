package net.swofty.type.skyblockgeneric.garden.visitor;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VisitorCatalogue {
    private static final Path DIRECTORY = Path.of("./configuration/skyblock/garden");
    private static VisitorCatalogue instance;

    public record Requirement(@Nullable String text, String kind, @Nullable Integer count) {
    }

    public record DesireItem(@Nullable String item, String name, @Nullable Integer amount) {
    }

    public record Desire(String mode, List<String> families, int count, List<DesireItem> items) {
    }

    public record Reward(String type, @Nullable String item, String name, double amount, @Nullable Double chancePercent,
                         @Nullable String note) {
    }

    public record Visitor(String id, String name, VisitorRarity rarity, int gardenLevel, Requirement requirement,
                          @Nullable String event, Desire desire, List<Reward> rewards, List<String> offerLines,
                          List<String> acceptLines, List<String> declineLines, boolean available,
                          @Nullable String unavailableReason) {
        public String coloured() {
            return rarity.getColour() + name;
        }
    }

    public record FamilyItem(String item, String name, double rawUnits) {
    }

    public record Family(String id, double multiplier, List<FamilyItem> items) {
        public FamilyItem top() {
            return items.getLast();
        }
    }

    public record MilestoneTier(int tier, int visitors, int cumulative, int farmingXp, int gardenXp, int skyblockXp) {
    }

    public record RareDrop(String item, double chancePercent, VisitorRarity minRarity) {
    }

    public record Cooldown(int uniqueServed, double minutes) {
    }

    private final Map<String, Visitor> visitors = new LinkedHashMap<>();
    private final Map<Integer, int[]> baseQuantity = new LinkedHashMap<>();
    private final Map<VisitorRarity, Double> quantityMultiplier = new EnumMap<>(VisitorRarity.class);
    private final Map<VisitorRarity, Double> weights = new EnumMap<>(VisitorRarity.class);
    private final Map<Integer, Integer> gardenXpByLevel = new LinkedHashMap<>();
    private final Map<VisitorRarity, Double> gardenXpMultiplier = new EnumMap<>(VisitorRarity.class);
    private final Map<VisitorRarity, Double> farmingXpMultiplier = new EnumMap<>(VisitorRarity.class);
    private final Map<VisitorRarity, Double> copperMultiplier = new EnumMap<>(VisitorRarity.class);
    private final Map<String, Family> families = new LinkedHashMap<>();
    private final List<Cooldown> cooldowns = new ArrayList<>();
    private final List<MilestoneTier> uniqueMilestones = new ArrayList<>();
    private final List<MilestoneTier> acceptedMilestones = new ArrayList<>();
    private final List<RareDrop> rareDrops = new ArrayList<>();
    private final List<String> queueChatter = new ArrayList<>();
    private final int bits;

    private VisitorCatalogue(Path directory) {
        JSONObject tables = read(directory.resolve("visitor_tables.json"));
        JSONObject base = tables.getJSONObject("baseQuantity");
        for (String key : base.keySet()) {
            JSONArray range = base.getJSONArray(key);
            baseQuantity.put(Integer.parseInt(key), new int[]{range.getInt(0), range.getInt(1)});
        }
        rarityMap(tables.getJSONObject("rarityQuantityMultiplier"), quantityMultiplier);
        rarityMap(tables.getJSONObject("rarityWeights"), weights);
        rarityMap(tables.getJSONObject("gardenXpRarityMultiplier"), gardenXpMultiplier);
        rarityMap(tables.getJSONObject("farmingXpRarityMultiplier"), farmingXpMultiplier);
        rarityMap(tables.getJSONObject("copperRarityMultiplier"), copperMultiplier);
        JSONObject xp = tables.getJSONObject("gardenXpByLevel");
        for (String key : xp.keySet()) {
            gardenXpByLevel.put(Integer.parseInt(key), xp.getInt(key));
        }
        bits = tables.optInt("bits", 5);
        for (Object entry : tables.getJSONArray("cooldownMinutes")) {
            JSONObject json = (JSONObject) entry;
            cooldowns.add(new Cooldown(json.getInt("uniqueServed"), json.getDouble("minutes")));
        }
        JSONObject familyRoot = tables.getJSONObject("families");
        for (String key : familyRoot.keySet()) {
            JSONObject json = familyRoot.getJSONObject(key);
            List<FamilyItem> items = new ArrayList<>();
            for (Object item : json.getJSONArray("items")) {
                JSONObject itemJson = (JSONObject) item;
                items.add(new FamilyItem(itemJson.getString("item"), itemJson.optString("name", itemJson.getString("item")),
                        itemJson.getDouble("rawUnits")));
            }
            families.put(key, new Family(key, json.getDouble("multiplier"), items));
        }
        milestones(tables.getJSONArray("uniqueMilestones"), uniqueMilestones);
        milestones(tables.getJSONArray("acceptedMilestones"), acceptedMilestones);
        for (Object entry : tables.optJSONArray("globalRareDrops") == null ? new JSONArray() : tables.getJSONArray("globalRareDrops")) {
            JSONObject json = (JSONObject) entry;
            rareDrops.add(new RareDrop(json.getString("item"), json.getDouble("chancePercent"),
                    VisitorRarity.valueOf(json.optString("minRarity", "UNCOMMON"))));
        }
        if (tables.optJSONArray("queueChatter") != null) {
            tables.getJSONArray("queueChatter").forEach(line -> queueChatter.add((String) line));
        }

        JSONObject root = read(directory.resolve("visitors.json"));
        for (Object entry : root.getJSONArray("visitors")) {
            Visitor visitor = parse((JSONObject) entry);
            visitors.put(visitor.id(), visitor);
        }
    }

    private static Visitor parse(JSONObject json) {
        JSONObject requirement = json.optJSONObject("requirement");
        Requirement parsedRequirement = requirement == null
                ? new Requirement(null, "NONE", null)
                : new Requirement(nullable(requirement, "text"), requirement.optString("kind", "NONE"),
                requirement.has("count") && !requirement.isNull("count") ? requirement.getInt("count") : null);
        JSONObject desire = json.getJSONObject("desire");
        List<DesireItem> desireItems = new ArrayList<>();
        for (Object item : desire.optJSONArray("items") == null ? new JSONArray() : desire.getJSONArray("items")) {
            JSONObject itemJson = (JSONObject) item;
            desireItems.add(new DesireItem(nullable(itemJson, "item"), itemJson.optString("name", ""),
                    itemJson.has("amount") && !itemJson.isNull("amount") ? itemJson.getInt("amount") : null));
        }
        List<String> families = new ArrayList<>();
        if (desire.optJSONArray("families") != null) {
            desire.getJSONArray("families").forEach(family -> families.add((String) family));
        }
        List<Reward> rewards = new ArrayList<>();
        for (Object entry : json.optJSONArray("rewards") == null ? new JSONArray() : json.getJSONArray("rewards")) {
            JSONObject reward = (JSONObject) entry;
            rewards.add(new Reward(reward.getString("type"), nullable(reward, "item"), reward.optString("name", ""),
                    reward.optDouble("amount", 1),
                    reward.has("chancePercent") && !reward.isNull("chancePercent") ? reward.getDouble("chancePercent") : null,
                    nullable(reward, "note")));
        }
        JSONObject dialogue = json.optJSONObject("dialogue");
        return new Visitor(json.getString("id"), json.getString("name"), VisitorRarity.valueOf(json.getString("rarity")),
                json.getInt("gardenLevel"), parsedRequirement, nullable(json, "event"),
                new Desire(desire.optString("mode", "UNKNOWN"), families, desire.optInt("count", 1), desireItems), rewards,
                lines(dialogue, "offer"), lines(dialogue, "accept"), lines(dialogue, "decline"),
                json.optBoolean("available", true), nullable(json, "unavailableReason"));
    }

    private static List<String> lines(@Nullable JSONObject dialogue, String key) {
        List<String> lines = new ArrayList<>();
        if (dialogue != null && dialogue.optJSONArray(key) != null) {
            dialogue.getJSONArray(key).forEach(line -> lines.add((String) line));
        }
        return lines;
    }

    private static @Nullable String nullable(JSONObject json, String key) {
        return json.has(key) && !json.isNull(key) ? json.getString(key) : null;
    }

    private static void rarityMap(JSONObject json, Map<VisitorRarity, Double> target) {
        for (String key : json.keySet()) {
            target.put(VisitorRarity.valueOf(key), json.getDouble(key));
        }
    }

    private static void milestones(JSONArray array, List<MilestoneTier> target) {
        for (Object entry : array) {
            JSONObject json = (JSONObject) entry;
            target.add(new MilestoneTier(json.getInt("tier"), json.optInt("visitors", 0), json.optInt("cumulative", 0),
                    json.getInt("farmingXp"), json.getInt("gardenXp"), json.getInt("skyblockXp")));
        }
    }

    private static JSONObject read(Path path) {
        try {
            return new JSONObject(Files.readString(path));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read " + path, exception);
        }
    }

    public static synchronized VisitorCatalogue get() {
        if (instance == null) {
            instance = new VisitorCatalogue(DIRECTORY);
        }
        return instance;
    }

    public static synchronized void load(Path directory) {
        instance = new VisitorCatalogue(directory);
    }

    public List<Visitor> visitors() {
        return List.copyOf(visitors.values());
    }

    public @Nullable Visitor visitor(String id) {
        return visitors.get(id);
    }

    public int[] baseQuantity(int gardenLevel) {
        return baseQuantity.get(Math.max(1, Math.min(15, gardenLevel)));
    }

    public double quantityMultiplier(VisitorRarity rarity) {
        return quantityMultiplier.get(rarity);
    }

    public double weight(VisitorRarity rarity) {
        return weights.getOrDefault(rarity, 0D);
    }

    public int gardenXp(int gardenLevel, VisitorRarity rarity) {
        int base = gardenXpByLevel.get(Math.max(1, Math.min(15, gardenLevel)));
        return (int) Math.round(base * gardenXpMultiplier.get(rarity));
    }

    public double farmingXpMultiplier(VisitorRarity rarity) {
        return farmingXpMultiplier.get(rarity);
    }

    public double copperMultiplier(VisitorRarity rarity) {
        return copperMultiplier.get(rarity);
    }

    public @Nullable Family family(String id) {
        return families.get(id);
    }

    public Map<String, Family> families() {
        return families;
    }

    public double cooldownMinutes(int uniqueServed) {
        double minutes = cooldowns.getFirst().minutes();
        for (Cooldown cooldown : cooldowns) {
            if (uniqueServed >= cooldown.uniqueServed()) minutes = cooldown.minutes();
        }
        return minutes;
    }

    public List<MilestoneTier> uniqueMilestones() {
        return uniqueMilestones;
    }

    public List<MilestoneTier> acceptedMilestones() {
        return acceptedMilestones;
    }

    public List<RareDrop> rareDrops() {
        return rareDrops;
    }

    public List<String> queueChatter() {
        return queueChatter;
    }

    public int bits() {
        return bits;
    }
}
