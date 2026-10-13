package net.swofty.type.skyblockgeneric.garden.composter;

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

public final class ComposterConfig {
    private static final Path FILE = Path.of("./configuration/skyblock/garden/composter.json");
    private static ComposterConfig instance;

    public record Item(String item, int amount) {
    }

    public record Tier(int tier, double value, List<Item> items, int copper, @Nullable Item rareCrop, int skyblockXp) {
    }

    private final Map<String, Double> organicMatter = new LinkedHashMap<>();
    private final Map<String, Double> fuel = new LinkedHashMap<>();
    private final Map<ComposterUpgrade, List<Tier>> upgrades = new EnumMap<>(ComposterUpgrade.class);
    private final Map<ComposterUpgrade, Integer> gardenLevelRequired = new EnumMap<>(ComposterUpgrade.class);
    private final double baseOrganicMatterCap;
    private final double baseFuelCap;
    private final double baseSeconds;
    private final double matterPerCompost;
    private final double fuelPerCompost;
    private final double tutorialMatter;
    private final double tutorialFuel;
    private final double tutorialFirstCycleSeconds;
    private final double tutorialFirstCycleFuel;

    private ComposterConfig(Path file) {
        JSONObject root;
        try {
            root = new JSONObject(Files.readString(file));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read " + file, exception);
        }
        root.getJSONObject("organicMatter").toMap().forEach((id, value) -> organicMatter.put(id, ((Number) value).doubleValue()));
        root.getJSONObject("fuel").toMap().forEach((id, value) -> fuel.put(id, ((Number) value).doubleValue()));
        JSONObject base = root.getJSONObject("base");
        baseOrganicMatterCap = base.getDouble("organicMatterCap");
        baseFuelCap = base.getDouble("fuelCap");
        baseSeconds = base.getDouble("secondsPerCompost");
        matterPerCompost = base.getDouble("matterPerCompost");
        fuelPerCompost = base.getDouble("fuelPerCompost");
        JSONObject tutorial = root.getJSONObject("tutorial");
        tutorialMatter = tutorial.getDouble("organicMatter");
        tutorialFuel = tutorial.getDouble("fuel");
        tutorialFirstCycleSeconds = tutorial.getDouble("firstCycleSeconds");
        tutorialFirstCycleFuel = tutorial.optDouble("firstCycleFuel", 3000);
        JSONObject levels = root.getJSONObject("gardenLevelRequired");
        List<JSONObject> tierCosts = new ArrayList<>();
        for (Object entry : root.getJSONArray("tierCosts")) {
            tierCosts.add((JSONObject) entry);
        }
        for (ComposterUpgrade upgrade : ComposterUpgrade.values()) {
            gardenLevelRequired.put(upgrade, levels.getInt(upgrade.name()));
            List<Tier> tiers = new ArrayList<>();
            JSONArray array = root.getJSONObject("upgrades").getJSONArray(upgrade.name());
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                JSONObject cost = tierCosts.get(i);
                List<Item> items = new ArrayList<>();
                for (Object item : json.getJSONArray("items")) {
                    JSONObject itemJson = (JSONObject) item;
                    items.add(new Item(itemJson.getString("item"), itemJson.getInt("amount")));
                }
                JSONObject rare = cost.optJSONObject("rareCrop");
                tiers.add(new Tier(json.getInt("tier"), json.getDouble("value"), items, cost.getInt("copper"),
                        rare == null ? null : new Item(rare.getString("item"), rare.getInt("amount")), cost.getInt("skyblockXp")));
            }
            upgrades.put(upgrade, tiers);
        }
    }

    public static synchronized ComposterConfig get() {
        if (instance == null) {
            instance = new ComposterConfig(FILE);
        }
        return instance;
    }

    public static synchronized void load(Path file) {
        instance = new ComposterConfig(file);
    }

    public double organicMatterValue(String item) {
        return organicMatter.getOrDefault(item, 0D);
    }

    public double fuelValue(String item) {
        return fuel.getOrDefault(item, 0D);
    }

    public Map<String, Double> organicMatter() {
        return organicMatter;
    }

    public Map<String, Double> fuel() {
        return fuel;
    }

    public @Nullable Tier tier(ComposterUpgrade upgrade, int tier) {
        List<Tier> tiers = upgrades.get(upgrade);
        return tier >= 1 && tier <= tiers.size() ? tiers.get(tier - 1) : null;
    }

    public int levelRequired(ComposterUpgrade upgrade) {
        return gardenLevelRequired.get(upgrade);
    }

    public int skyblockXp(int[] tiers) {
        int total = 0;
        for (ComposterUpgrade upgrade : ComposterUpgrade.values()) {
            for (int tier = 1; tier <= tiers[upgrade.ordinal()]; tier++) {
                total += tier(upgrade, tier).skyblockXp();
            }
        }
        return total;
    }

    public double baseOrganicMatterCap() {
        return baseOrganicMatterCap;
    }

    public double baseFuelCap() {
        return baseFuelCap;
    }

    public double baseSeconds() {
        return baseSeconds;
    }

    public double matterPerCompost() {
        return matterPerCompost;
    }

    public double fuelPerCompost() {
        return fuelPerCompost;
    }

    public double tutorialMatter() {
        return tutorialMatter;
    }

    public double tutorialFuel() {
        return tutorialFuel;
    }

    public double tutorialFirstCycleSeconds() {
        return tutorialFirstCycleSeconds;
    }

    public double tutorialFirstCycleFuel() {
        return tutorialFirstCycleFuel;
    }
}
