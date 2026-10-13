package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GreenhouseConfig {
    private static final Path FILE = Path.of("./configuration/skyblock/garden/greenhouse.json");
    public static final String DEAD_PLANT = "DEAD_PLANT";
    private static GreenhouseConfig instance;

    public enum Upgrade {
        GROWTH_SPEED("Growth Speed"), PLANT_YIELD("Plant Yield"), PLOT_LIMIT("Plot Limit");

        private final String displayName;

        Upgrade(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    public record Cost(int copper, int bundles, int vines, int xp) {
    }

    public record Bounty(String item, double chancePercent) {
    }

    private final JSONObject json;
    private final Map<String, PlantType> plants = new LinkedHashMap<>();
    private final Map<Upgrade, List<Cost>> upgrades = new LinkedHashMap<>();
    private final List<Bounty> bounty = new ArrayList<>();

    private GreenhouseConfig(JSONObject json) {
        this.json = json;
        JSONObject crops = json.getJSONObject("crops");
        for (String key : crops.keySet()) {
            JSONObject crop = crops.getJSONObject(key);
            GardenCrop garden = GardenCrop.valueOf(key);
            List<GreenhouseEffect> effects = new ArrayList<>();
            JSONArray array = crop.getJSONArray("effects");
            for (int i = 0; i < array.length(); i++) effects.add(GreenhouseEffect.valueOf(array.getString(i)));
            plants.put(key, new PlantType(key, garden.getDisplayName(), garden, false, crop.getInt("stages"),
                    crop.getInt("yield"), effects, key.equals("SUGAR_CANE") ? "Sand/Dirt" : key.equals("NETHER_WART") ? "Soul Sand" : "Farmland", null));
        }
        plants.put(DEAD_PLANT, new PlantType(DEAD_PLANT, "Dead Plant", null, true, 0, 1, List.of(), "Farmland", "COMMON"));
        JSONObject all = json.getJSONObject("upgrades");
        for (Upgrade upgrade : Upgrade.values()) {
            JSONArray tiers = all.getJSONArray(upgrade.name());
            List<Cost> costs = new ArrayList<>();
            for (int i = 0; i < tiers.length(); i++) {
                JSONObject tier = tiers.getJSONObject(i);
                costs.add(new Cost(tier.optInt("copper"), tier.optInt("bundles"), tier.optInt("vines"), tier.optInt("xp")));
            }
            upgrades.put(upgrade, costs);
        }
        JSONArray drops = json.getJSONArray("bounty");
        for (int i = 0; i < drops.length(); i++) {
            JSONArray row = drops.getJSONArray(i);
            bounty.add(new Bounty(row.getString(0), row.getDouble(1)));
        }
    }

    public static synchronized GreenhouseConfig get() {
        if (instance == null) {
            try {
                instance = new GreenhouseConfig(new JSONObject(Files.readString(FILE)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return instance;
    }

    public synchronized void register(PlantType type) {
        plants.put(type.id(), type);
    }

    public @Nullable PlantType plant(String id) {
        return plants.get(id);
    }

    public @Nullable PlantType plantOf(GardenCrop crop) {
        return plants.get(crop.name());
    }

    public List<PlantType> plants() {
        return List.copyOf(plants.values());
    }

    public List<Cost> costs(Upgrade upgrade) {
        return upgrades.get(upgrade);
    }

    public int maxTier(Upgrade upgrade) {
        return upgrades.get(upgrade).size();
    }

    public List<Bounty> bounty() {
        return bounty;
    }

    public double stageHours() {
        return json.getDouble("stageHours");
    }

    public int baseXp() {
        return json.getInt("baseXp");
    }

    public double growthSpeedPercent(int tier) {
        return json.getJSONArray("growthSpeed").getDouble(Math.min(tier, json.getJSONArray("growthSpeed").length() - 1));
    }

    public double plantYieldPercent(int tier) {
        return json.getJSONArray("plantYield").getDouble(Math.min(tier, json.getJSONArray("plantYield").length() - 1));
    }

    public double uniqueCropPercent() {
        return json.getDouble("uniqueCropPercent");
    }

    public int maxUniqueCrops() {
        return json.getInt("maxUniqueCrops");
    }

    public int waterPerLevel() {
        return json.getInt("waterPerLevel");
    }

    public int waterLossMin() {
        return json.getInt("waterLossMin");
    }

    public int waterLossMax() {
        return json.getInt("waterLossMax");
    }

    public double unwateredAdvanceChance() {
        return json.getDouble("unwateredAdvanceChance");
    }

    public int decayDaysSinceWatering() {
        return json.getInt("decayDaysSinceWatering");
    }

    public int decayDaysUnwaterable() {
        return json.getInt("decayDaysUnwaterable");
    }

    public int[] starterColumns() {
        JSONArray array = json.getJSONObject("starterSlots").getJSONArray("columns");
        return new int[]{array.getInt(0), array.getInt(1)};
    }

    public int[] starterRows() {
        JSONArray array = json.getJSONObject("starterSlots").getJSONArray("rows");
        return new int[]{array.getInt(0), array.getInt(1)};
    }

    public double vineChance(String rarity) {
        return json.getJSONObject("vineChance").optDouble(rarity, 0);
    }
}
