package net.swofty.type.skyblockgeneric.crimson.kuudra.reward;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Vesuvius's storage: every won run's chests, newest first, kept per profile for 72 hours and capped at
 * 60 runs (the oldest are dropped). Opened runs stay listed until they expire, as on Hypixel.
 */
public final class KuudraChestStorage {
    public static final String SECTION = "kuudra_chests";
    public static final int MAX_RUNS = 60;

    private KuudraChestStorage() {
    }

    public static List<KuudraRun> runs(SkyBlockPlayer player) {
        return alive(parse(DatapointCrimsonIsle.of(player).getSection(SECTION)), System.currentTimeMillis());
    }

    public static @Nullable KuudraRun find(SkyBlockPlayer player, String id) {
        return runs(player).stream().filter(run -> run.id().equals(id)).findFirst().orElse(null);
    }

    public static void store(SkyBlockPlayer player, KuudraRun run) {
        save(player, withRun(runs(player), run, System.currentTimeMillis()));
    }

    public static void replace(SkyBlockPlayer player, KuudraRun run) {
        List<KuudraRun> runs = new ArrayList<>(runs(player));
        runs.replaceAll(each -> each.id().equals(run.id()) ? run : each);
        save(player, runs);
    }

    public static void clear(SkyBlockPlayer player) {
        save(player, List.of());
    }

    static List<KuudraRun> withRun(List<KuudraRun> runs, KuudraRun run, long now) {
        List<KuudraRun> updated = new ArrayList<>(alive(runs, now));
        updated.removeIf(each -> each.id().equals(run.id()));
        updated.add(run);
        updated.sort(Comparator.comparingLong(KuudraRun::completedAt).reversed());
        return List.copyOf(updated.subList(0, Math.min(MAX_RUNS, updated.size())));
    }

    static List<KuudraRun> alive(List<KuudraRun> runs, long now) {
        return runs.stream().filter(run -> !run.isExpired(now)).toList();
    }

    static List<KuudraRun> parse(@Nullable String section) {
        if (section == null || section.isBlank()) return List.of();
        JSONArray array = new JSONArray(section);
        List<KuudraRun> runs = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) runs.add(KuudraRun.fromJson(array.getJSONObject(i)));
        return runs;
    }

    static String serialize(List<KuudraRun> runs) {
        JSONArray array = new JSONArray();
        runs.forEach(run -> array.put(run.toJson()));
        return array.toString();
    }

    private static void save(SkyBlockPlayer player, List<KuudraRun> runs) {
        String serialized = serialize(runs);
        DatapointCrimsonIsle.edit(player, data -> data.getSections().put(SECTION, serialized));
    }
}
