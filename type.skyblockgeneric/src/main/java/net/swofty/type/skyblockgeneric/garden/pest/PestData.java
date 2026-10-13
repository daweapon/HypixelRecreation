package net.swofty.type.skyblockgeneric.garden.pest;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Getter
@Setter
public final class PestData {
    public static final int MAX_ALIVE = 8;

    @Getter
    public static final class Alive {
        private final String id;
        private final String type;
        private final int plot;

        public Alive(String id, String type, int plot) {
            this.id = id;
            this.type = type;
            this.plot = plot;
        }

        public static Alive create(String type, int plot) {
            return new Alive(UUID.randomUUID().toString(), type, plot);
        }
    }

    public record Spray(String material, long until) {
    }

    @Getter
    @Setter
    public static final class Trap {
        private final String id;
        private final String kind;
        private final int plot;
        private final double x;
        private final double y;
        private final double z;
        private String material = "";
        private int bait;
        private long nextAt;
        private final List<String> caught = new ArrayList<>();

        public Trap(String id, String kind, int plot, double x, double y, double z) {
            this.id = id;
            this.kind = kind;
            this.plot = plot;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private final List<Alive> alive = new ArrayList<>();
    private final List<Trap> traps = new ArrayList<>();
    private final Map<Integer, Spray> sprays = new TreeMap<>();
    private final Map<String, Integer> killedByType = new TreeMap<>();
    private int stored;
    private long totalKilled;
    private long cooldownUntil;
    private long repellentUntil;
    private boolean repellentMax;
    private String selectedSpray = "COMPOST";
    private long boostUntil;
    private int boostFortune;
    private int permanentBonusChance;
    private long lastTick;
    private boolean metPhillip;
    private boolean metPamela;

    public int aliveCount() {
        return alive.size();
    }

    public int aliveIn(int plot) {
        return (int) alive.stream().filter(pest -> pest.plot == plot).count();
    }

    public boolean removeAlive(String id) {
        return alive.removeIf(pest -> pest.id.equals(id));
    }

    public void addKill(String type) {
        totalKilled++;
        killedByType.merge(type, 1, Integer::sum);
    }

    public Spray sprayOn(int plot, long now) {
        Spray spray = sprays.get(plot);
        if (spray != null && spray.until() <= now) {
            sprays.remove(plot);
            return null;
        }
        return spray;
    }

    public JSONObject toJson() {
        JSONArray pests = new JSONArray();
        alive.forEach(pest -> pests.put(new JSONObject().put("id", pest.id).put("type", pest.type).put("plot", pest.plot)));
        JSONObject sprayJson = new JSONObject();
        sprays.forEach((plot, spray) -> sprayJson.put(String.valueOf(plot),
                new JSONObject().put("material", spray.material()).put("until", spray.until())));
        JSONArray trapJson = new JSONArray();
        traps.forEach(trap -> trapJson.put(new JSONObject().put("id", trap.id).put("kind", trap.kind).put("plot", trap.plot)
                .put("x", trap.x).put("y", trap.y).put("z", trap.z).put("material", trap.material).put("bait", trap.bait)
                .put("nextAt", trap.nextAt).put("caught", new JSONArray(trap.caught))));
        return new JSONObject().put("traps", trapJson).put("alive", pests).put("sprays", sprayJson).put("killedByType", new JSONObject(killedByType))
                .put("stored", stored).put("totalKilled", totalKilled).put("cooldownUntil", cooldownUntil)
                .put("repellentUntil", repellentUntil).put("repellentMax", repellentMax)
                .put("selectedSpray", selectedSpray).put("boostUntil", boostUntil).put("boostFortune", boostFortune)
                .put("permanentBonusChance", permanentBonusChance).put("lastTick", lastTick)
                .put("metPhillip", metPhillip).put("metPamela", metPamela);
    }

    public static PestData fromJson(JSONObject json) {
        PestData data = new PestData();
        if (json == null) return data;
        JSONArray pests = json.optJSONArray("alive");
        if (pests != null) {
            for (int i = 0; i < pests.length(); i++) {
                JSONObject pest = pests.getJSONObject(i);
                data.alive.add(new Alive(pest.getString("id"), pest.getString("type"), pest.getInt("plot")));
            }
        }
        JSONArray trapJson = json.optJSONArray("traps");
        if (trapJson != null) {
            for (int i = 0; i < trapJson.length(); i++) {
                JSONObject t = trapJson.getJSONObject(i);
                Trap trap = new Trap(t.getString("id"), t.getString("kind"), t.getInt("plot"), t.getDouble("x"), t.getDouble("y"), t.getDouble("z"));
                trap.material = t.optString("material", "");
                trap.bait = t.optInt("bait", 0);
                trap.nextAt = t.optLong("nextAt", 0);
                JSONArray caught = t.optJSONArray("caught");
                if (caught != null) for (int c = 0; c < caught.length(); c++) trap.caught.add(caught.getString(c));
                data.traps.add(trap);
            }
        }
        JSONObject sprays = json.optJSONObject("sprays");
        if (sprays != null) {
            for (String key : sprays.keySet()) {
                JSONObject spray = sprays.getJSONObject(key);
                data.sprays.put(Integer.parseInt(key), new Spray(spray.getString("material"), spray.getLong("until")));
            }
        }
        JSONObject killed = json.optJSONObject("killedByType");
        if (killed != null) {
            for (String key : killed.keySet()) data.killedByType.put(key, killed.getInt(key));
        }
        data.stored = json.optInt("stored", 0);
        data.totalKilled = json.optLong("totalKilled", 0);
        data.cooldownUntil = json.optLong("cooldownUntil", 0);
        data.repellentUntil = json.optLong("repellentUntil", 0);
        data.repellentMax = json.optBoolean("repellentMax", false);
        data.selectedSpray = json.optString("selectedSpray", "COMPOST");
        data.boostUntil = json.optLong("boostUntil", 0);
        data.boostFortune = json.optInt("boostFortune", 0);
        data.permanentBonusChance = json.optInt("permanentBonusChance", 0);
        data.lastTick = json.optLong("lastTick", 0);
        data.metPhillip = json.optBoolean("metPhillip", false);
        data.metPamela = json.optBoolean("metPamela", false);
        return data;
    }
}
