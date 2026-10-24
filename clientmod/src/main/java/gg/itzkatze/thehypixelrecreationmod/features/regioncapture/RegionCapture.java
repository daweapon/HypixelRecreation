package gg.itzkatze.thehypixelrecreationmod.features.regioncapture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gg.itzkatze.thehypixelrecreationmod.utils.StringUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Map;

public final class RegionCapture {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int SAMPLE_SPACING = 3;
    private static final int PADDING = 1;
    private static final int MOB_SCAN_TICKS = 10;
    private static final int MOB_LIMIT = 20_000;
    private static final double LABEL_RANGE = 1.8;
    private static final double LABEL_HEIGHT = 3.5;

    private static boolean active;
    private static String name;
    private static String serverType;
    private static long startedAt;
    private static String current;
    private static int[] lastSample;
    private static final Map<String, Zone> ZONES = new LinkedHashMap<>();
    private static final List<Transition> TRANSITIONS = new ArrayList<>();
    private static final List<String> NOTES = new ArrayList<>();
    private static final Set<UUID> SEEN_MOBS = new HashSet<>();
    private static final List<MobSighting> MOBS = new ArrayList<>();
    private static long ticks;

    private RegionCapture() {
    }

    public record MobSighting(long millis, String zone, String type, String label, double x, double y, double z) {
    }

    public record Transition(long millis, String from, String to, int x, int y, int z) {
    }

    private static final class Zone {
        final String name;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        int samples;
        final List<int[]> points = new ArrayList<>();
        final List<int[]> edges = new ArrayList<>();

        Zone(String name) {
            this.name = name;
        }

        void include(int x, int y, int z) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
            samples++;
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static synchronized void start(String sessionName, String server) {
        if (active) throw new IllegalStateException("A region capture is already running");
        active = true;
        name = sessionName;
        serverType = server;
        startedAt = System.currentTimeMillis();
        current = null;
        lastSample = null;
        ZONES.clear();
        TRANSITIONS.clear();
        NOTES.clear();
        SEEN_MOBS.clear();
        MOBS.clear();
        ticks = 0;
    }

    public static synchronized void note(String text) {
        if (!active) throw new IllegalStateException("No region capture is running");
        NOTES.add(text);
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && current != null) {
            Zone zone = ZONES.get(current);
            if (zone != null) zone.edges.add(new int[]{client.player.blockPosition().getX(), client.player.blockPosition().getY(), client.player.blockPosition().getZ()});
        }
    }

    public static synchronized void tick() {
        if (!active) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        int x = client.player.blockPosition().getX();
        int y = client.player.blockPosition().getY();
        int z = client.player.blockPosition().getZ();
        String zoneName = zoneName(client);
        if (zoneName == null) return;

        if (!zoneName.equals(current)) {
            TRANSITIONS.add(new Transition(System.currentTimeMillis() - startedAt, current, zoneName, x, y, z));
            if (current != null) {
                Zone previous = ZONES.get(current);
                previous.include(x, y, z);
                previous.edges.add(new int[]{x, y, z});
            }
            Zone next = ZONES.computeIfAbsent(zoneName, Zone::new);
            next.edges.add(new int[]{x, y, z});
            current = zoneName;
        }

        if (++ticks % MOB_SCAN_TICKS == 0) scanMobs(client, zoneName);

        Zone zone = ZONES.get(zoneName);
        zone.include(x, y, z);
        if (lastSample == null || Math.abs(lastSample[0] - x) + Math.abs(lastSample[1] - y) + Math.abs(lastSample[2] - z) >= SAMPLE_SPACING) {
            zone.points.add(new int[]{x, y, z});
            lastSample = new int[]{x, y, z};
        }
    }

    private static void scanMobs(Minecraft client, String zoneName) {
        if (MOBS.size() >= MOB_LIMIT) return;
        List<Entity> all = new ArrayList<>();
        client.level.entitiesForRendering().forEach(all::add);
        for (Entity entity : all) {
            if (!(entity instanceof LivingEntity) || entity instanceof Player || entity instanceof ArmorStand) continue;
            if (!SEEN_MOBS.add(entity.getUUID())) continue;
            MOBS.add(new MobSighting(System.currentTimeMillis() - startedAt, zoneName,
                    BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath(), labelFor(entity, all),
                    entity.getX(), entity.getY(), entity.getZ()));
        }
    }

    private static String labelFor(Entity mob, List<Entity> all) {
        String own = mob.getCustomName() == null ? null : StringUtility.stripColor(StringUtility.toLegacyString(mob.getCustomName()));
        if (own != null && !own.isBlank()) return own;
        String best = "";
        double bestDistance = Double.MAX_VALUE;
        for (Entity other : all) {
            if (other == mob || other.getCustomName() == null) continue;
            double dx = other.getX() - mob.getX();
            double dz = other.getZ() - mob.getZ();
            double dy = other.getY() - mob.getY();
            double distance = dx * dx + dz * dz;
            if (distance > LABEL_RANGE * LABEL_RANGE || dy < -0.5 || dy > LABEL_HEIGHT || distance >= bestDistance) continue;
            String text = StringUtility.stripColor(StringUtility.toLegacyString(other.getCustomName()));
            if (text.isBlank()) continue;
            best = text;
            bestDistance = distance;
        }
        return best;
    }

    private static String mobName(String label, String type) {
        String name = label.replaceAll("\\[[^\\]]*\\]", "").replaceAll("[0-9][0-9,.kKmM]*/[0-9,.kKmM]+\\S*", "").trim();
        return name.isEmpty() ? type : name;
    }

    public static String describe() {
        StringBuilder text = new StringBuilder("§aRegion capture §f" + name + "§a: §f" + ZONES.size() + "§a zones, §f" + TRANSITIONS.size() + "§a transitions");
        for (Zone zone : ZONES.values()) {
            text.append("\n§7").append(zone.name).append(" §f").append(zone.samples).append("§7 samples, x ")
                    .append(zone.minX).append("..").append(zone.maxX).append(" y ").append(zone.minY).append("..").append(zone.maxY)
                    .append(" z ").append(zone.minZ).append("..").append(zone.maxZ);
        }
        text.append("\n§7mobs seen: §f").append(MOBS.size());
        text.append("\n§7now in: §f").append(current == null ? "no zone line found yet" : current);
        return text.toString();
    }

    public static synchronized Path stop() throws IOException {
        if (!active) throw new IllegalStateException("No region capture is running");
        Path directory = write();
        active = false;
        return directory;
    }

    public static synchronized Path export() throws IOException {
        if (!active) throw new IllegalStateException("No region capture is running");
        return write();
    }

    private static Path write() throws IOException {
        Path directory = Minecraft.getInstance().gameDirectory.toPath().resolve("region-captures");
        Files.createDirectories(directory);
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path base = directory.resolve(name + "_" + stamp);
        Files.writeString(base.resolveSibling(base.getFileName() + ".json"), GSON.toJson(toJson()));
        Files.writeString(base.resolveSibling(base.getFileName() + ".yml"), toYaml());
        return base;
    }

    private static JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("name", name);
        root.addProperty("serverType", serverType);
        JsonArray zones = new JsonArray();
        for (Zone zone : ZONES.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", zone.name);
            entry.addProperty("samples", zone.samples);
            entry.add("min", array(zone.minX, zone.minY, zone.minZ));
            entry.add("max", array(zone.maxX, zone.maxY, zone.maxZ));
            JsonArray edges = new JsonArray();
            zone.edges.forEach(edge -> edges.add(array(edge[0], edge[1], edge[2])));
            entry.add("edges", edges);
            JsonArray points = new JsonArray();
            zone.points.forEach(point -> points.add(array(point[0], point[1], point[2])));
            entry.add("points", points);
            zones.add(entry);
        }
        root.add("zones", zones);
        JsonArray transitions = new JsonArray();
        for (Transition transition : TRANSITIONS) {
            JsonObject entry = new JsonObject();
            entry.addProperty("seconds", transition.millis() / 1000.0);
            entry.addProperty("from", transition.from());
            entry.addProperty("to", transition.to());
            entry.add("at", array(transition.x(), transition.y(), transition.z()));
            transitions.add(entry);
        }
        root.add("transitions", transitions);
        JsonArray mobs = new JsonArray();
        Map<String, JsonObject> summary = new TreeMap<>();
        for (MobSighting mob : MOBS) {
            JsonObject entry = new JsonObject();
            entry.addProperty("seconds", mob.millis() / 1000.0);
            entry.addProperty("zone", mob.zone());
            entry.addProperty("type", mob.type());
            entry.addProperty("label", mob.label());
            entry.add("at", array((int) Math.floor(mob.x()), (int) Math.floor(mob.y()), (int) Math.floor(mob.z())));
            mobs.add(entry);
            String key = mob.zone() + " | " + mobName(mob.label(), mob.type());
            JsonObject group = summary.computeIfAbsent(key, ignored -> {
                JsonObject created = new JsonObject();
                created.addProperty("count", 0);
                created.add("min", array(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
                created.add("max", array(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE));
                return created;
            });
            group.addProperty("count", group.get("count").getAsInt() + 1);
            widen(group.getAsJsonArray("min"), group.getAsJsonArray("max"), (int) Math.floor(mob.x()), (int) Math.floor(mob.y()), (int) Math.floor(mob.z()));
        }
        root.add("mobSummary", GSON.toJsonTree(summary));
        root.add("mobs", mobs);
        JsonArray notes = new JsonArray();
        NOTES.forEach(notes::add);
        root.add("notes", notes);
        return root;
    }

    private static void widen(JsonArray min, JsonArray max, int x, int y, int z) {
        int[] at = {x, y, z};
        for (int i = 0; i < 3; i++) {
            min.set(i, new com.google.gson.JsonPrimitive(Math.min(min.get(i).getAsInt(), at[i])));
            max.set(i, new com.google.gson.JsonPrimitive(Math.max(max.get(i).getAsInt(), at[i])));
        }
    }

    private static JsonArray array(int x, int y, int z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    private static String toYaml() {
        StringBuilder yaml = new StringBuilder("regions:\n");
        for (Zone zone : ZONES.values()) {
            String type = zone.name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
            yaml.append("  - id: ").append(type.toLowerCase(Locale.ROOT)).append('\n')
                    .append("    type: ").append(type).append('\n')
                    .append("    x1: ").append(zone.minX - PADDING).append('\n')
                    .append("    y1: ").append(zone.minY - PADDING).append('\n')
                    .append("    z1: ").append(zone.minZ - PADDING).append('\n')
                    .append("    x2: ").append(zone.maxX + PADDING).append('\n')
                    .append("    y2: ").append(zone.maxY + PADDING).append('\n')
                    .append("    z2: ").append(zone.maxZ + PADDING).append('\n')
                    .append("    serverType: ").append(serverType).append('\n');
        }
        return yaml.toString();
    }

    static String zoneName(Minecraft client) {
        Scoreboard scoreboard = client.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return null;
        List<PlayerScoreEntry> entries = scoreboard.listPlayerScores(objective).stream()
                .sorted(Comparator.comparingInt(PlayerScoreEntry::value).reversed()).toList();
        for (PlayerScoreEntry entry : entries) {
            String line = StringUtility.stripColor(StringUtility.toLegacyString(render(scoreboard, entry)));
            String zone = zoneFrom(line);
            if (zone != null) return zone;
        }
        return null;
    }

    static String zoneFrom(String line) {
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '⏣' || c == 'ф' || (c >= '' && c <= '')) {
                String zone = line.substring(i + 1).trim();
                return zone.isEmpty() ? null : zone;
            }
        }
        return null;
    }

    private static Component render(Scoreboard scoreboard, PlayerScoreEntry entry) {
        Component base = entry.display() != null ? entry.display() : Component.literal(entry.owner());
        PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
        if (team == null) return base;
        return Component.empty().append(team.getPlayerPrefix()).append(base).append(team.getPlayerSuffix());
    }
}
