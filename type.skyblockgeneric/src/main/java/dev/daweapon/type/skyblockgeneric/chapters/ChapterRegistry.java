package dev.daweapon.type.skyblockgeneric.chapters;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ChapterRegistry {
    private static final Map<String, SkillCategories> SKILL_KEYS = Map.of(
            "foraging", SkillCategories.FORAGING, "hunting", SkillCategories.HUNTING,
            "combat", SkillCategories.COMBAT, "fishing", SkillCategories.FISHING);
    private static final Map<ChapterIsland, ChapterRegistry> LOADED = new ConcurrentHashMap<>();

    private final ChapterIsland island;
    private final List<Chapter> chapters;

    private ChapterRegistry(ChapterIsland island, List<Chapter> chapters) {
        this.island = island;
        this.chapters = chapters;
    }

    public static ChapterRegistry get() {
        return of(ChapterIsland.MOONGLADE_MARSH);
    }

    public static ChapterRegistry of(ChapterIsland island) {
        return LOADED.computeIfAbsent(island, key -> load(key.file(), key));
    }

    public static List<ChapterRegistry> all() {
        return Arrays.stream(ChapterIsland.values()).map(ChapterRegistry::of).toList();
    }

    public static ChapterRegistry load(Path file) {
        return load(file, ChapterIsland.MOONGLADE_MARSH);
    }

    @SuppressWarnings("unchecked")
    public static ChapterRegistry load(Path file, ChapterIsland island) {
        Map<String, Object> root;
        try (InputStream stream = Files.newInputStream(file)) {
            root = new Yaml().load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + file, exception);
        }
        List<Chapter> chapters = new ArrayList<>();
        for (Map<String, Object> entry : (List<Map<String, Object>>) root.get("chapters")) {
            int number = ((Number) entry.get("number")).intValue();
            List<ChapterTask> tasks = new ArrayList<>();
            for (Map<String, Object> task : (List<Map<String, Object>>) entry.get("tasks")) tasks.add(parseTask(task));
            chapters.add(new Chapter(island, number, (String) entry.get("description"), List.copyOf(tasks),
                    parseReward((Map<String, Object>) entry.get("completion"))));
        }
        return new ChapterRegistry(island, List.copyOf(chapters));
    }

    @SuppressWarnings("unchecked")
    private static ChapterTask parseTask(Map<String, Object> task) {
        String trigger = (String) task.get("trigger");
        String live = (String) task.get("live");
        long amount = task.get("amount") instanceof Number number ? number.longValue() : 1;
        ChapterProbe probe = live == null ? null : ChapterProbe.parse(live);
        return new ChapterTask((String) task.get("id"), (String) task.get("title"), (String) task.get("description"),
                parseReward((Map<String, Object>) task.get("rewards")), trigger, probe, amount,
                !Boolean.FALSE.equals(task.get("available")), (String) task.get("unavailable"));
    }

    @SuppressWarnings("unchecked")
    private static ChapterReward parseReward(Map<String, Object> reward) {
        Map<SkillCategories, Long> skills = new LinkedHashMap<>();
        SKILL_KEYS.forEach((key, skill) -> {
            if (reward.get(key) instanceof Number amount) skills.put(skill, amount.longValue());
        });
        Map<String, Integer> shards = new LinkedHashMap<>();
        Map<String, Object> shardEntries = (Map<String, Object>) reward.getOrDefault("shards", Map.of());
        shardEntries.forEach((name, amount) -> {
            String shard = AttributeRegistry.findByShard(name).orElseThrow(() -> new IllegalStateException("Unknown shard " + name)).shard();
            shards.put(shard, ((Number) amount).intValue());
        });
        Map<ItemType, Integer> items = new LinkedHashMap<>();
        Map<String, Object> itemEntries = (Map<String, Object>) reward.getOrDefault("items", Map.of());
        itemEntries.forEach((name, amount) -> items.put(ItemType.valueOf(name), ((Number) amount).intValue()));
        return new ChapterReward(number(reward, "hotf"), skills, number(reward, "forestEssence"),
                (int) number(reward, "skyblockXp"), shards, items, number(reward, "whispers"),
                number(reward, "desertWhispers"), number(reward, "safariEssence"));
    }

    private static long number(Map<String, Object> reward, String key) {
        return reward.get(key) instanceof Number value ? value.longValue() : 0;
    }

    public ChapterIsland island() {
        return island;
    }

    public List<Chapter> chapters() {
        return chapters;
    }

    public Chapter chapter(int number) {
        return chapters.get(number - 1);
    }

    public int taskCount() {
        return chapters.stream().mapToInt(chapter -> chapter.tasks().size()).sum();
    }
}
