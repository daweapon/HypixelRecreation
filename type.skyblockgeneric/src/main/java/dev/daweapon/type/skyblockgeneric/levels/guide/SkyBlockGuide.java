package dev.daweapon.type.skyblockgeneric.levels.guide;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryBag;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import dev.daweapon.type.skyblockgeneric.levels.LevelsGuide;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelCause;
import dev.daweapon.type.skyblockgeneric.levels.causes.NewAccessoryLevelCause;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SkyBlockGuide {
    public static final double UNLOCK_THRESHOLD = 0.5;
    private static final String RESOURCE = "/skyblock_guide.json";

    private static final long SNAPSHOT_MILLIS = 1_000;
    private static final Map<UUID, Snapshot> SNAPSHOTS = new ConcurrentHashMap<>();

    private static volatile Map<LevelsGuide, List<GuideTask>> tasks;

    private record Snapshot(Set<GuideEntry> complete, long takenAt) {
    }

    private SkyBlockGuide() {
    }

    public static List<GuideTask> tasks(LevelsGuide tier) {
        return catalogue().getOrDefault(tier, List.of());
    }

    public static List<GuideEntry> entries(LevelsGuide tier) {
        return tasks(tier).stream().flatMap(task -> task.entries().stream()).toList();
    }

    public static boolean complete(SkyBlockPlayer player, GuideEntry entry) {
        return entry.available() && snapshot(player).contains(entry);
    }

    private static Set<GuideEntry> snapshot(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        Snapshot current = SNAPSHOTS.get(player.getUuid());
        if (current != null && now - current.takenAt() < SNAPSHOT_MILLIS) return current.complete();
        GuideScan scan = new GuideScan(player);
        Set<GuideEntry> complete = Collections.newSetFromMap(new IdentityHashMap<>());
        for (List<GuideTask> tierTasks : catalogue().values()) {
            for (GuideTask task : tierTasks) {
                for (GuideEntry entry : task.entries()) {
                    if (entry.rule().complete(scan)) complete.add(entry);
                }
            }
        }
        SNAPSHOTS.values().removeIf(snapshot -> now - snapshot.takenAt() >= SNAPSHOT_MILLIS);
        SNAPSHOTS.put(player.getUuid(), new Snapshot(complete, now));
        return complete;
    }

    public static long completed(SkyBlockPlayer player, LevelsGuide tier) {
        return entries(tier).stream().filter(entry -> entry.complete(player)).count();
    }

    public static double progress(SkyBlockPlayer player, LevelsGuide tier) {
        List<GuideEntry> entries = entries(tier);
        if (entries.isEmpty()) return 0;
        return completed(player, tier) / (double) entries.size();
    }

    public static boolean unlocked(SkyBlockPlayer player, LevelsGuide tier) {
        if (tier.ordinal() == 0) return true;
        LevelsGuide previous = LevelsGuide.values()[tier.ordinal() - 1];
        return !tasks(previous).isEmpty() && progress(player, previous) >= UNLOCK_THRESHOLD;
    }

    public static LevelsGuide currentTier(SkyBlockPlayer player) {
        LevelsGuide current = LevelsGuide.STARTER;
        for (LevelsGuide candidate : LevelsGuide.values()) {
            if (tasks(candidate).isEmpty() || !unlocked(player, candidate)) break;
            current = candidate;
        }
        return current;
    }

    public static void maxOut(SkyBlockPlayer player) {
        DatapointAccessoryBag.PlayerAccessoryBag bag = player.getAccessoryBag();
        for (LevelsGuide tier : LevelsGuide.values()) {
            for (GuideTask task : tasks(tier)) {
                if (task.kind() != GuideKind.ACCESSORY) continue;
                for (GuideEntry entry : task.entries()) discover(player, bag, entry);
            }
        }
        AccessoryBag.save(player, bag);
        SNAPSHOTS.remove(player.getUuid());
    }

    private static void discover(SkyBlockPlayer player, DatapointAccessoryBag.PlayerAccessoryBag bag, GuideEntry entry) {
        ItemType type = entry.available() ? ItemType.get(entry.ref()) : null;
        if (type == null || bag.hasDiscoveredAccessory(type)) return;
        bag.addDiscoveredAccessory(type);
        NewAccessoryLevelCause cause = SkyBlockLevelCause.getAccessoryCause(type);
        if (cause != null) player.getSkyBlockExperience().addExperience(cause);
    }

    private static Map<LevelsGuide, List<GuideTask>> catalogue() {
        Map<LevelsGuide, List<GuideTask>> current = tasks;
        if (current == null) {
            synchronized (SkyBlockGuide.class) {
                if (tasks == null) tasks = load();
                current = tasks;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private static Map<LevelsGuide, List<GuideTask>> load() {
        LoaderOptions options = new LoaderOptions();
        options.setCodePointLimit(Integer.MAX_VALUE);
        try (InputStream stream = SkyBlockGuide.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) throw new IllegalStateException("Missing " + RESOURCE);
            Map<String, Object> root = new Yaml(options).load(stream);
            Map<String, List<Map<String, Object>>> raw = (Map<String, List<Map<String, Object>>>) root.get("tiers");
            Map<LevelsGuide, List<GuideTask>> loaded = new EnumMap<>(LevelsGuide.class);
            raw.forEach((tier, groups) -> {
                List<GuideTask> parsed = new ArrayList<>();
                for (Map<String, Object> group : groups) parsed.add(parse(group));
                loaded.put(LevelsGuide.valueOf(tier), List.copyOf(parsed));
            });
            return loaded;
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not read " + RESOURCE, exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static GuideTask parse(Map<String, Object> group) {
        GuideKind kind = GuideKind.valueOf((String) group.get("kind"));
        List<GuideEntry> entries = new ArrayList<>();
        for (Map<String, Object> entry : (List<Map<String, Object>>) group.get("entries")) {
            String ref = (String) entry.get("ref");
            entries.add(new GuideEntry((String) entry.get("label"), ref, kind, GuideRules.of(kind, ref)));
        }
        return new GuideTask((String) group.get("name"), (String) group.get("task"), (String) group.get("icon"), kind,
                (String) group.get("description"), ((Number) group.get("xp")).intValue(), List.copyOf(entries));
    }
}
