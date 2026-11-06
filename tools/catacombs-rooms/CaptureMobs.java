import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Where a captured run's dungeon mobs first stood, read from streams/entities.jsonl: each mob's first position, its
 * name and star from the Hypixel name tag above it (the armour stand with the next entity id, else the nearest one just
 * above it), and the Floor 1 mob it stands for. Higher floors swap the same spots for stronger mobs of the same build,
 * so an M7 Zombie Knight becomes a Zombie Grunt and keeps its star.
 */
final class CaptureMobs {
    static final Set<String> MOB_TYPES = Set.of("minecraft:zombie", "minecraft:skeleton", "minecraft:wither_skeleton",
            "minecraft:spider", "minecraft:cave_spider", "minecraft:creeper", "minecraft:enderman", "minecraft:player");
    static final Map<String, String> FLOOR_ONE_IDS = new LinkedHashMap<>();
    private static final Pattern COLOUR = Pattern.compile("§.|\\p{Co}");
    private static final Pattern LEVEL = Pattern.compile("^\\[Lv\\d+]\\s*");
    private static final Pattern HEALTH = Pattern.compile("\\s*[\\d,.]+[kMB]?(/[\\d,.]+[kMB]?)?❤\\s*$");
    private static final Pattern AFFIX = Pattern.compile("^(Healthy|Speedy|Flaming|Fortified|Healing|Stormy|Golden|Boomer|Stealthy) ");

    static {
        FLOOR_ONE_IDS.put("Zombie Grunt", "ZOMBIE_GRUNT");
        FLOOR_ONE_IDS.put("Zombie Soldier", "ZOMBIE_GRUNT");
        FLOOR_ONE_IDS.put("Zombie Knight", "ZOMBIE_GRUNT");
        FLOOR_ONE_IDS.put("Zombie Commander", "ZOMBIE_GRUNT");
        FLOOR_ONE_IDS.put("Zombie Lord", "ZOMBIE_GRUNT");
        FLOOR_ONE_IDS.put("Tank Zombie", "TANK_ZOMBIE");
        FLOOR_ONE_IDS.put("Super Tank Zombie", "TANK_ZOMBIE");
        FLOOR_ONE_IDS.put("Skeleton Grunt", "SKELETON_GRUNT");
        FLOOR_ONE_IDS.put("Super Archer", "SKELETON_GRUNT");
        FLOOR_ONE_IDS.put("Scared Skeleton", "SCARED_SKELETON");
        FLOOR_ONE_IDS.put("Skeleton Soldier", "SKELETON_SOLDIER");
        FLOOR_ONE_IDS.put("Skeleton Master", "SKELETON_SOLDIER");
        FLOOR_ONE_IDS.put("Skeleton Lord", "SKELETON_SOLDIER");
        FLOOR_ONE_IDS.put("Skeletor", "SKELETON_SOLDIER");
        FLOOR_ONE_IDS.put("Sniper", "SNIPER");
        FLOOR_ONE_IDS.put("Crypt Lurker", "CRYPT_LURKER");
        FLOOR_ONE_IDS.put("Crypt Dreadlord", "CRYPT_DREADLORD");
        FLOOR_ONE_IDS.put("Withermancer", "CRYPT_DREADLORD");
        FLOOR_ONE_IDS.put("Crypt Souleater", "CRYPT_SOULEATER");
        FLOOR_ONE_IDS.put("Fels", "CRYPT_SOULEATER");
        FLOOR_ONE_IDS.put("Cellar Spider", "CELLAR_SPIDER");
        FLOOR_ONE_IDS.put("Lonely Spider", "LONELY_SPIDER");
        FLOOR_ONE_IDS.put("Lost Adventurer", "LOST_ADVENTURER");
        FLOOR_ONE_IDS.put("Frozen Adventurer", "LOST_ADVENTURER");
        FLOOR_ONE_IDS.put("Shadow Assassin", "LOST_ADVENTURER");
        FLOOR_ONE_IDS.put("Angry Archaeologist", "ANGRY_ARCHAEOLOGIST");
    }

    private CaptureMobs() {
    }

    record Mob(String seenAs, String mobId, double x, double y, double z, boolean starred) {
    }

    private record Added(String type, long ms, double x, double y, double z) {
    }

    @SuppressWarnings("unchecked")
    static List<Mob> read(Path capture) throws IOException {
        Map<Integer, Added> added = new LinkedHashMap<>();
        Map<Integer, List<String>> names = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(capture.resolve("streams").resolve("entities.jsonl"), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("\"world\":\"world_002\"")) continue;
                boolean add = line.contains("\"type\":\"entity:add\"");
                boolean named = line.contains("❤") && (line.contains("\"type\":\"entity:data\"") || line.contains("\"type\":\"entity:detail\""));
                if (!add && !named) continue;
                Map<String, Object> event = (Map<String, Object>) Json.parse(line);
                Map<String, Object> data = (Map<String, Object>) event.get("data");
                int id = ((Double) data.get("id")).intValue();
                if (add) {
                    added.putIfAbsent(id, new Added((String) data.get("type"), ((Double) event.get("ms")).longValue(),
                            (Double) data.get("x"), (Double) data.get("y"), (Double) data.get("z")));
                    continue;
                }
                Object name = event.get("type").equals("entity:data")
                        ? ((Map<String, Object>) data.get("values")).get("2") : data.get("customName");
                if (name instanceof Map<?, ?> text && text.get("legacy") instanceof String legacy && legacy.contains("❤")) {
                    names.computeIfAbsent(id, ignored -> new ArrayList<>()).add(legacy);
                }
            }
        }
        List<Mob> mobs = new ArrayList<>();
        for (Map.Entry<Integer, Added> entry : added.entrySet()) {
            Added mob = entry.getValue();
            if (!MOB_TYPES.contains(mob.type())) continue;
            List<String> tag = tag(entry.getKey(), mob, added, names);
            if (tag == null) continue;
            String name = baseName(tag.getFirst());
            String mobId = FLOOR_ONE_IDS.get(name);
            if (mobId == null) continue;
            boolean starred = tag.stream().anyMatch(text -> text.contains("✯"));
            mobs.add(new Mob(name, mobId, mob.x(), mob.y(), mob.z(), starred));
        }
        return mobs;
    }

    private static List<String> tag(int id, Added mob, Map<Integer, Added> added, Map<Integer, List<String>> names) {
        Added next = added.get(id + 1);
        if (next != null && next.type().equals("minecraft:armor_stand") && names.containsKey(id + 1)) return names.get(id + 1);
        List<String> best = null;
        double bestDistance = 1;
        for (Map.Entry<Integer, List<String>> entry : names.entrySet()) {
            Added stand = added.get(entry.getKey());
            if (stand == null || !stand.type().equals("minecraft:armor_stand") || Math.abs(stand.ms() - mob.ms()) > 3000) continue;
            double dy = stand.y() - mob.y();
            double distance = Math.pow(stand.x() - mob.x(), 2) + Math.pow(stand.z() - mob.z(), 2);
            if (dy > 0 && dy < 4 && distance < bestDistance) {
                bestDistance = distance;
                best = entry.getValue();
            }
        }
        return best;
    }

    static String baseName(String legacy) {
        String plain = COLOUR.matcher(legacy).replaceAll("").replace("✯", "").trim();
        plain = LEVEL.matcher(plain).replaceFirst("");
        plain = HEALTH.matcher(plain).replaceFirst("").trim();
        return AFFIX.matcher(plain).replaceFirst("");
    }
}
