package dev.daweapon.type.skyblockgeneric.dungeons.reward;

import dev.daweapon.dungeons.catacombs.reward.DungeonChestRules;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Croesus's storage: every won run's chests, newest first, kept per profile for 72 hours and capped at 60 runs
 * (the oldest are dropped). Opened runs stay listed until they expire. Also remembers whether the player has
 * heard Croesus's introduction.
 */
public final class DungeonChestStorage {
    public static final int MAX_RUNS = DungeonChestRules.CROESUS_RETAINED_RUNS;

    private DungeonChestStorage() {
    }

    public static List<DungeonChestRun> runs(SkyBlockPlayer player) {
        return alive(parse(root(player).optJSONArray("runs")), System.currentTimeMillis());
    }

    public static @Nullable DungeonChestRun find(SkyBlockPlayer player, String id) {
        return runs(player).stream().filter(run -> run.id().equals(id)).findFirst().orElse(null);
    }

    public static void store(SkyBlockPlayer player, DungeonChestRun run) {
        save(player, withRun(runs(player), run, System.currentTimeMillis()));
    }

    public static void replace(SkyBlockPlayer player, DungeonChestRun run) {
        List<DungeonChestRun> runs = new ArrayList<>(runs(player));
        runs.replaceAll(each -> each.id().equals(run.id()) ? run : each);
        save(player, runs);
    }

    public static void clear(SkyBlockPlayer player) {
        save(player, List.of());
    }

    public static boolean hasMetCroesus(SkyBlockPlayer player) {
        return root(player).optBoolean("metCroesus", false);
    }

    public static void meetCroesus(SkyBlockPlayer player) {
        JSONObject root = root(player);
        point(player).setValue(root.put("metCroesus", true).toString());
    }

    static List<DungeonChestRun> withRun(List<DungeonChestRun> runs, DungeonChestRun run, long now) {
        List<DungeonChestRun> updated = new ArrayList<>(alive(runs, now));
        updated.removeIf(each -> each.id().equals(run.id()));
        updated.add(run);
        updated.sort(Comparator.comparingLong(DungeonChestRun::completedAt).reversed());
        return List.copyOf(updated.subList(0, Math.min(MAX_RUNS, updated.size())));
    }

    static List<DungeonChestRun> alive(List<DungeonChestRun> runs, long now) {
        return runs.stream().filter(run -> !run.isExpired(now)).toList();
    }

    static List<DungeonChestRun> parse(@Nullable JSONArray array) {
        if (array == null) return List.of();
        List<DungeonChestRun> runs = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) runs.add(DungeonChestRun.fromJson(array.getJSONObject(i)));
        return runs;
    }

    static JSONArray serialize(List<DungeonChestRun> runs) {
        JSONArray array = new JSONArray();
        runs.forEach(run -> array.put(run.toJson()));
        return array;
    }

    private static JSONObject root(SkyBlockPlayer player) {
        String value = point(player).getValue();
        return value == null || value.isBlank() ? new JSONObject() : new JSONObject(value);
    }

    private static void save(SkyBlockPlayer player, List<DungeonChestRun> runs) {
        point(player).setValue(root(player).put("runs", serialize(runs)).toString());
    }

    private static DatapointString point(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CATACOMBS_CHESTS, DatapointString.class);
    }
}
