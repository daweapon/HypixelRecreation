package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.commons.YamlFileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HuntingHints {
    public static final File DIRECTORY = new File("./configuration/skyblock/hunting");
    private static final Map<String, List<Hint>> CACHE = new ConcurrentHashMap<>();

    private HuntingHints() {
    }

    public record Hint(String id, String name, String plural, String shard, long coins, List<String> lines) {
    }

    public static List<Hint> get(String fileName) {
        return CACHE.computeIfAbsent(fileName, key -> load(new File(DIRECTORY, key + ".yml")));
    }

    @SuppressWarnings("unchecked")
    public static List<Hint> load(File file) {
        try {
            List<Map<String, Object>> raw = (List<Map<String, Object>>) YamlFileUtils.loadYaml(file).get("hints");
            return raw.stream().map(entry -> {
                String name = String.valueOf(entry.get("name"));
                return new Hint(String.valueOf(entry.get("id")), name,
                        String.valueOf(entry.getOrDefault("plural", name + "s")),
                        String.valueOf(entry.get("shard")), ((Number) entry.get("coins")).longValue(),
                        ((List<?>) entry.get("lines")).stream().map(String::valueOf).toList());
            }).toList();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    public static String dialogueKey(Hint hint) {
        return "hint-" + hint.id();
    }

    public static Hint byId(List<Hint> hints, String id) {
        return hints.stream().filter(hint -> hint.id().equals(id)).findFirst().orElse(null);
    }
}
