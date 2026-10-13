package net.swofty.type.skyblockgeneric.garden;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class GardenConfig {
    private static final Path DIRECTORY = Path.of("./configuration/skyblock/garden");
    private static GardenConfig instance;

    public record Level(int level, double cumulativeXp, int newVisitors, List<GardenCrop> crops, List<String> pests,
                        int cropUpgradeTier, String barnSkin, List<String> extras, int skyblockXp, int cropGrowth) {
    }

    public record MilestoneTier(int tier, int gardenXp, int farmingXp, int skyblockXp) {
    }

    public record UpgradeTier(int tier, int gardenLevel, int copper) {
    }

    public record PlotGroup(int minGardenLevel, List<Integer> costs, List<Integer> plots) {
    }

    private final List<Level> levels;
    private final List<MilestoneTier> milestoneTiers;
    private final Map<GardenCrop, long[]> cumulativeThresholds = new EnumMap<>(GardenCrop.class);
    private final List<UpgradeTier> upgradeTiers;
    private final int fortunePerUpgradeTier;
    private final List<PlotGroup> plotGroups;
    private final int skyblockXpPerPlot;
    private final int compostPerBundle;

    private GardenConfig(Path directory) {
        JSONObject levelRoot = read(directory.resolve("levels.json"));
        levels = new ArrayList<>();
        for (Object entry : levelRoot.getJSONArray("levels")) {
            JSONObject json = (JSONObject) entry;
            List<GardenCrop> crops = new ArrayList<>();
            json.getJSONArray("crops").forEach(crop -> crops.add(GardenCrop.valueOf((String) crop)));
            levels.add(new Level(json.getInt("level"), json.getDouble("cumulativeXp"), json.getInt("newVisitors"), crops,
                    strings(json.getJSONArray("pests")), json.getInt("cropUpgradeTier"), json.getString("barnSkin"),
                    strings(json.getJSONArray("extras")), json.getInt("skyblockXp"), json.getInt("cropGrowth")));
        }

        JSONObject milestoneRoot = read(directory.resolve("crop_milestones.json"));
        milestoneTiers = new ArrayList<>();
        for (Object entry : milestoneRoot.getJSONArray("tiers")) {
            JSONObject json = (JSONObject) entry;
            milestoneTiers.add(new MilestoneTier(json.getInt("tier"), json.getInt("gardenXp"), json.getInt("farmingXp"), json.getInt("skyblockXp")));
        }
        JSONObject thresholds = milestoneRoot.getJSONObject("thresholds");
        for (GardenCrop crop : GardenCrop.values()) {
            JSONArray perTier = thresholds.getJSONArray(crop.name());
            long[] cumulative = new long[perTier.length()];
            long running = 0;
            for (int i = 0; i < perTier.length(); i++) {
                running += perTier.getLong(i);
                cumulative[i] = running;
            }
            cumulativeThresholds.put(crop, cumulative);
        }

        JSONObject upgradeRoot = read(directory.resolve("crop_upgrades.json"));
        fortunePerUpgradeTier = upgradeRoot.getInt("fortunePerTier");
        upgradeTiers = new ArrayList<>();
        for (Object entry : upgradeRoot.getJSONArray("tiers")) {
            JSONObject json = (JSONObject) entry;
            upgradeTiers.add(new UpgradeTier(json.getInt("tier"), json.getInt("gardenLevel"), json.getInt("copper")));
        }

        JSONObject plotRoot = read(directory.resolve("plots.json"));
        skyblockXpPerPlot = plotRoot.getInt("skyblockXpPerPlot");
        compostPerBundle = plotRoot.getInt("compostPerBundle");
        plotGroups = new ArrayList<>();
        for (Object entry : plotRoot.getJSONArray("groups")) {
            JSONObject json = (JSONObject) entry;
            plotGroups.add(new PlotGroup(json.getInt("minGardenLevel"), integers(json.getJSONArray("costs")), integers(json.getJSONArray("plots"))));
        }
    }

    public static synchronized GardenConfig get() {
        if (instance == null) {
            instance = new GardenConfig(DIRECTORY);
        }
        return instance;
    }

    public static synchronized void load(Path directory) {
        instance = new GardenConfig(directory);
    }

    public List<Level> levels() {
        return levels;
    }

    public int maxLevel() {
        return levels.size();
    }

    public Level level(int level) {
        return levels.get(level - 1);
    }

    public List<MilestoneTier> milestoneTiers() {
        return milestoneTiers;
    }

    public int maxMilestoneTier() {
        return milestoneTiers.size();
    }

    public long cumulativeThreshold(GardenCrop crop, int tier) {
        return cumulativeThresholds.get(crop)[tier - 1];
    }

    public long tierThreshold(GardenCrop crop, int tier) {
        return tier == 1 ? cumulativeThreshold(crop, 1) : cumulativeThreshold(crop, tier) - cumulativeThreshold(crop, tier - 1);
    }

    public List<UpgradeTier> upgradeTiers() {
        return upgradeTiers;
    }

    public int maxUpgradeTier() {
        return upgradeTiers.size();
    }

    public int fortunePerUpgradeTier() {
        return fortunePerUpgradeTier;
    }

    public List<PlotGroup> plotGroups() {
        return plotGroups;
    }

    public int skyblockXpPerPlot() {
        return skyblockXpPerPlot;
    }

    public int compostPerBundle() {
        return compostPerBundle;
    }

    private static JSONObject read(Path file) {
        try {
            return new JSONObject(Files.readString(file));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + file, e);
        }
    }

    private static List<String> strings(JSONArray array) {
        List<String> values = new ArrayList<>();
        array.forEach(value -> values.add((String) value));
        return values;
    }

    private static List<Integer> integers(JSONArray array) {
        List<Integer> values = new ArrayList<>();
        array.forEach(value -> values.add(((Number) value).intValue()));
        return values;
    }
}
