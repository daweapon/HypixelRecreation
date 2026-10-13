package net.swofty.type.skyblockgeneric.garden;

import lombok.Getter;
import lombok.Setter;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterState;
import net.swofty.type.skyblockgeneric.garden.chip.ChipData;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseData;
import net.swofty.type.skyblockgeneric.garden.pest.PestData;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorData;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

@Getter
public class GardenProfile {
    public static final int PLOT_COUNT = 24;
    public static final int DEFAULT_SPEED_CAP = 100;

    @Setter
    private double gardenXp;
    @Setter
    private boolean accessUnlocked;
    private double copper;
    private double sowdust;
    private final TreeSet<Integer> unlockedPlots = new TreeSet<>();
    private final TreeSet<Integer> cleanedPlots = new TreeSet<>();
    private final Map<Integer, Integer> cleanTotals = new TreeMap<>();
    private final Map<Integer, String> plotPresets = new TreeMap<>();
    private VisitorData visitors = new VisitorData();
    private ComposterState composter = new ComposterState();
    private PestData pests = new PestData();
    private GreenhouseData greenhouse = new GreenhouseData();
    private ChipData chips = new ChipData();
    private final Map<GardenCrop, Long> cropCounters = new EnumMap<>(GardenCrop.class);
    private final Map<GardenCrop, Integer> milestoneTiers = new EnumMap<>(GardenCrop.class);
    private final Map<GardenCrop, Integer> cropUpgradeTiers = new EnumMap<>(GardenCrop.class);
    private final Map<GardenCrop, Integer> speedCaps = new EnumMap<>(GardenCrop.class);

    public ComposterState composter() {
        return composter;
    }

    public ChipData chips() {
        return chips;
    }

    public GreenhouseData greenhouse() {
        return greenhouse;
    }

    public PestData pests() {
        return pests;
    }

    public VisitorData visitors() {
        return visitors;
    }

    public long cropCount(GardenCrop crop) {
        return cropCounters.getOrDefault(crop, 0L);
    }

    public long addCropCount(GardenCrop crop, long amount) {
        long total = cropCount(crop) + Math.max(0, amount);
        cropCounters.put(crop, total);
        return total;
    }

    public int milestoneTier(GardenCrop crop) {
        return milestoneTiers.getOrDefault(crop, 0);
    }

    public void setMilestoneTier(GardenCrop crop, int tier) {
        milestoneTiers.put(crop, Math.max(0, tier));
    }

    public int cropUpgradeTier(GardenCrop crop) {
        return cropUpgradeTiers.getOrDefault(crop, 0);
    }

    public void setCropUpgradeTier(GardenCrop crop, int tier) {
        cropUpgradeTiers.put(crop, Math.max(0, tier));
    }

    public int speedCap(GardenCrop crop, int defaultCap) {
        return speedCaps.getOrDefault(crop, defaultCap);
    }

    public void setSpeedCap(GardenCrop crop, int cap) {
        speedCaps.put(crop, cap);
    }

    public void resetSpeedCap(GardenCrop crop) {
        speedCaps.remove(crop);
    }

    public boolean isPlotUnlocked(int plot) {
        return unlockedPlots.contains(plot);
    }

    public boolean unlockPlot(int plot) {
        return plot >= 1 && plot <= PLOT_COUNT && unlockedPlots.add(plot);
    }

    public boolean isPlotCleaned(int plot) {
        return cleanedPlots.contains(plot);
    }

    public boolean markPlotCleaned(int plot) {
        return cleanedPlots.add(plot);
    }

    public int cleanTotal(int plot) {
        return cleanTotals.getOrDefault(plot, 0);
    }

    public void setCleanTotal(int plot, int total) {
        cleanTotals.put(plot, total);
    }

    public String plotPreset(int plot, String defaultPreset) {
        return plotPresets.getOrDefault(plot, defaultPreset);
    }

    public void setPlotPreset(int plot, String preset) {
        plotPresets.put(plot, preset);
    }

    public void addCopper(double amount) {
        copper = Math.max(0, copper + amount);
    }

    public boolean spendCopper(double amount) {
        if (amount < 0 || copper < amount) {
            return false;
        }
        copper -= amount;
        return true;
    }

    public void addSowdust(double amount) {
        sowdust = Math.max(0, sowdust + amount);
    }

    public boolean spendSowdust(double amount) {
        if (amount < 0 || sowdust < amount) {
            return false;
        }
        sowdust -= amount;
        return true;
    }

    public String serialize() {
        JSONObject json = new JSONObject();
        json.put("gardenXp", gardenXp);
        json.put("accessUnlocked", accessUnlocked);
        json.put("copper", copper);
        json.put("sowdust", sowdust);
        json.put("unlockedPlots", new JSONArray(unlockedPlots));
        json.put("cleanedPlots", new JSONArray(cleanedPlots));
        json.put("cleanTotals", presetsLikeJson(cleanTotals));
        json.put("plotPresets", presetsJson());
        json.put("visitors", visitors.toJson());
        json.put("composter", composter.toJson());
        json.put("pests", pests.toJson());
        json.put("greenhouse", greenhouse.toJson());
        json.put("chips", chips.toJson());
        json.put("cropCounters", cropJson(cropCounters));
        json.put("milestoneTiers", cropJson(milestoneTiers));
        json.put("cropUpgradeTiers", cropJson(cropUpgradeTiers));
        json.put("speedCaps", cropJson(speedCaps));
        return json.toString();
    }

    public static GardenProfile deserialize(@Nullable String serialized) {
        GardenProfile profile = new GardenProfile();
        if (serialized == null || serialized.isEmpty() || serialized.equals("null")) {
            return profile;
        }
        JSONObject json = new JSONObject(serialized);
        profile.gardenXp = json.optDouble("gardenXp", 0);
        profile.accessUnlocked = json.optBoolean("accessUnlocked", false);
        profile.copper = json.optDouble("copper", 0);
        profile.sowdust = json.optDouble("sowdust", 0);
        JSONArray plots = json.optJSONArray("unlockedPlots");
        if (plots != null) {
            for (int i = 0; i < plots.length(); i++) {
                profile.unlockedPlots.add(plots.getInt(i));
            }
        }
        JSONArray cleaned = json.optJSONArray("cleanedPlots");
        if (cleaned != null) {
            for (int i = 0; i < cleaned.length(); i++) {
                profile.cleanedPlots.add(cleaned.getInt(i));
            }
        }
        JSONObject totals = json.optJSONObject("cleanTotals");
        if (totals != null) {
            for (String key : totals.keySet()) {
                profile.cleanTotals.put(Integer.parseInt(key), totals.getInt(key));
            }
        }
        profile.visitors = VisitorData.fromJson(json.optJSONObject("visitors"));
        profile.composter = ComposterState.fromJson(json.optJSONObject("composter"));
        profile.pests = PestData.fromJson(json.optJSONObject("pests"));
        profile.greenhouse = GreenhouseData.fromJson(json.optJSONObject("greenhouse"));
        profile.chips = ChipData.fromJson(json.optJSONObject("chips"));
        JSONObject presets = json.optJSONObject("plotPresets");
        if (presets != null) {
            for (String key : presets.keySet()) {
                profile.plotPresets.put(Integer.parseInt(key), presets.getString(key));
            }
        }
        readCropMap(json.optJSONObject("cropCounters"), (crop, value) -> profile.cropCounters.put(crop, value.longValue()));
        readCropMap(json.optJSONObject("milestoneTiers"), (crop, value) -> profile.milestoneTiers.put(crop, value.intValue()));
        readCropMap(json.optJSONObject("cropUpgradeTiers"), (crop, value) -> profile.cropUpgradeTiers.put(crop, value.intValue()));
        readCropMap(json.optJSONObject("speedCaps"), (crop, value) -> profile.speedCaps.put(crop, value.intValue()));
        return profile;
    }

    public GardenProfile copy() {
        return deserialize(serialize());
    }

    private JSONObject presetsJson() {
        JSONObject json = new JSONObject();
        plotPresets.forEach((plot, preset) -> json.put(String.valueOf(plot), preset));
        return json;
    }

    private static JSONObject presetsLikeJson(Map<Integer, Integer> values) {
        JSONObject json = new JSONObject();
        values.forEach((plot, value) -> json.put(String.valueOf(plot), value));
        return json;
    }

    private static JSONObject cropJson(Map<GardenCrop, ? extends Number> values) {
        JSONObject json = new JSONObject();
        values.forEach((crop, value) -> json.put(crop.name(), value));
        return json;
    }

    private static void readCropMap(@Nullable JSONObject json, java.util.function.BiConsumer<GardenCrop, Number> sink) {
        if (json == null) {
            return;
        }
        for (String key : json.keySet()) {
            try {
                sink.accept(GardenCrop.valueOf(key), json.getNumber(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
