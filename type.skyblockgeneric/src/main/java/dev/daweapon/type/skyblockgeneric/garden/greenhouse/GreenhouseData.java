package dev.daweapon.type.skyblockgeneric.garden.greenhouse;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
public final class GreenhouseData {
    public static final int SIZE = 10;
    public static final int SLOTS = SIZE * SIZE;

    @Getter
    @Setter
    public static final class Plant {
        private final String type;
        private int stage;
        private double water;
        private long plantedAt;
        private long lastWateredAt;
        private long grownAt;
        private boolean bonusLocked;
        private final Map<String, Double> state = new TreeMap<>();

        public double state(String key) {
            return state.getOrDefault(key, 0.0);
        }

        public void setState(String key, double value) {
            if (value == 0) state.remove(key);
            else state.put(key, value);
        }

        public Plant(String type, long now) {
            this.type = type;
            this.plantedAt = now;
            this.lastWateredAt = now;
        }

        public boolean grown(PlantType plant) {
            return stage >= plant.stages();
        }
    }

    @Getter
    public static final class Bed {
        private final boolean[] unlocked = new boolean[SLOTS];
        private final Map<Integer, Plant> plants = new TreeMap<>();
        private final Surface[] surfaces = new Surface[SLOTS];

        public Surface surface(int slot) {
            return surfaces[slot] == null ? Surface.FARMLAND : surfaces[slot];
        }

        public void setSurface(int slot, Surface surface) {
            surfaces[slot] = surface == Surface.FARMLAND ? null : surface;
        }

        public int anchorAt(int slot) {
            if (plants.containsKey(slot)) return slot;
            GreenhouseConfig config = GreenhouseConfig.get();
            for (Map.Entry<Integer, Plant> entry : plants.entrySet()) {
                PlantType type = config.plant(entry.getValue().getType());
                int size = type == null ? 1 : type.footprint();
                if (size > 1 && Footprint.cells(entry.getKey(), size).contains(slot)) return entry.getKey();
            }
            return -1;
        }

        public Plant plantAt(int slot) {
            int anchor = anchorAt(slot);
            return anchor < 0 ? null : plants.get(anchor);
        }

        public boolean occupied(int slot) {
            return anchorAt(slot) >= 0;
        }

        public int unlockedCount() {
            int count = 0;
            for (boolean slot : unlocked) if (slot) count++;
            return count;
        }

        public boolean allUnlocked() {
            return unlockedCount() == SLOTS;
        }
    }

    private final Map<Integer, Bed> beds = new TreeMap<>();
    private final Map<String, Double> tanks = new TreeMap<>();
    private final java.util.Set<String> analyzed = new java.util.TreeSet<>();
    private boolean blueprintOffered;
    private boolean blueprintGiven;
    private boolean blueprintDelivered;
    private boolean resourcesDelivered;
    private int dnaClaimed;
    private int growthSpeedTier;
    private int plantYieldTier;
    private int plotLimitTier;
    private long nextStageAt;
    private long lastStageAt;
    private int stageCount;
    private int flasksConsumed;
    private long flaskReadyAt;

    public static int slot(int column, int row) {
        return row * SIZE + column;
    }

    public JSONObject toJson() {
        JSONObject bedJson = new JSONObject();
        beds.forEach((plot, bed) -> {
            StringBuilder mask = new StringBuilder();
            for (boolean slot : bed.unlocked) mask.append(slot ? '1' : '0');
            JSONArray plants = new JSONArray();
            bed.plants.forEach((slot, plant) -> plants.put(new JSONObject().put("slot", slot).put("type", plant.type)
                    .put("stage", plant.stage).put("water", plant.water).put("plantedAt", plant.plantedAt)
                    .put("lastWateredAt", plant.lastWateredAt).put("grownAt", plant.grownAt).put("locked", plant.bonusLocked)
                    .put("state", new JSONObject(plant.state))));
            JSONObject surfaces = new JSONObject();
            for (int i = 0; i < SLOTS; i++) if (bed.surfaces[i] != null) surfaces.put(String.valueOf(i), bed.surfaces[i].name());
            bedJson.put(String.valueOf(plot), new JSONObject().put("unlocked", mask.toString()).put("plants", plants).put("surfaces", surfaces));
        });
        return new JSONObject().put("beds", bedJson).put("blueprintOffered", blueprintOffered).put("blueprintGiven", blueprintGiven)
                .put("blueprintDelivered", blueprintDelivered).put("resourcesDelivered", resourcesDelivered)
                .put("dnaClaimed", dnaClaimed).put("growthSpeedTier", growthSpeedTier).put("plantYieldTier", plantYieldTier).put("plotLimitTier", plotLimitTier)
                .put("tanks", new JSONObject(tanks)).put("analyzed", new JSONArray(analyzed)).put("nextStageAt", nextStageAt).put("lastStageAt", lastStageAt).put("stageCount", stageCount)
                .put("flasksConsumed", flasksConsumed).put("flaskReadyAt", flaskReadyAt);
    }

    public static GreenhouseData fromJson(JSONObject json) {
        GreenhouseData data = new GreenhouseData();
        if (json == null) return data;
        JSONObject beds = json.optJSONObject("beds");
        if (beds != null) {
            for (String key : beds.keySet()) {
                JSONObject entry = beds.getJSONObject(key);
                Bed bed = new Bed();
                String mask = entry.optString("unlocked", "");
                for (int i = 0; i < Math.min(mask.length(), SLOTS); i++) bed.unlocked[i] = mask.charAt(i) == '1';
                JSONArray plants = entry.optJSONArray("plants");
                if (plants != null) {
                    for (int i = 0; i < plants.length(); i++) {
                        JSONObject p = plants.getJSONObject(i);
                        Plant plant = new Plant(p.getString("type"), p.optLong("plantedAt", 0));
                        plant.stage = p.optInt("stage", 0);
                        plant.water = p.optDouble("water", 0);
                        plant.lastWateredAt = p.optLong("lastWateredAt", 0);
                        plant.grownAt = p.optLong("grownAt", 0);
                        plant.bonusLocked = p.optBoolean("locked", false);
                        JSONObject state = p.optJSONObject("state");
                        if (state != null) for (String stateKey : state.keySet()) plant.state.put(stateKey, state.getDouble(stateKey));
                        bed.plants.put(p.getInt("slot"), plant);
                    }
                }
                JSONObject surfaces = entry.optJSONObject("surfaces");
                if (surfaces != null) {
                    for (String slot : surfaces.keySet()) {
                        int index = Integer.parseInt(slot);
                        Surface surface = Surface.parse(surfaces.getString(slot));
                        if (surface != null && index >= 0 && index < SLOTS) bed.surfaces[index] = surface;
                    }
                }
                data.beds.put(Integer.parseInt(key), bed);
            }
        }
        JSONObject tanks = json.optJSONObject("tanks");
        if (tanks != null) for (String key : tanks.keySet()) data.tanks.put(key, tanks.getDouble(key));
        JSONArray analyzed = json.optJSONArray("analyzed");
        if (analyzed != null) for (int i = 0; i < analyzed.length(); i++) data.analyzed.add(analyzed.getString(i));
        data.blueprintOffered = json.optBoolean("blueprintOffered", false);
        data.blueprintGiven = json.optBoolean("blueprintGiven", false);
        data.blueprintDelivered = json.optBoolean("blueprintDelivered", false);
        data.resourcesDelivered = json.optBoolean("resourcesDelivered", false);
        data.dnaClaimed = json.optInt("dnaClaimed", 0);
        data.growthSpeedTier = json.optInt("growthSpeedTier", 0);
        data.plantYieldTier = json.optInt("plantYieldTier", 0);
        data.plotLimitTier = json.optInt("plotLimitTier", 0);
        data.nextStageAt = json.optLong("nextStageAt", 0);
        data.lastStageAt = json.optLong("lastStageAt", 0);
        data.stageCount = json.optInt("stageCount", 0);
        data.flasksConsumed = json.optInt("flasksConsumed", 0);
        data.flaskReadyAt = json.optLong("flaskReadyAt", 0);
        return data;
    }

    public List<Integer> plots() {
        return new ArrayList<>(beds.keySet());
    }

    public Bed bed(int plot) {
        return beds.get(plot);
    }

    public int greenhouseLimit() {
        return 1 + plotLimitTier;
    }
}
