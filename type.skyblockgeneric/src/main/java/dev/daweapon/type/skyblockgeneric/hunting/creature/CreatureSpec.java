package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record CreatureSpec(
        String id,
        String name,
        String island,
        Kind kind,
        int level,
        double health,
        double damage,
        double speed,
        EntityType entity,
        List<MobType> mobTypes,
        @Nullable String shard,
        Capture capture,
        int staminaBars,
        NetTier minNet,
        @Nullable String special,
        Medium medium,
        Behavior behavior,
        Xp xp,
        int orbs,
        int coins,
        List<Drop> drops,
        double shardChance,
        Bestiary bestiary,
        Spawn spawn,
        List<Message> messages,
        String notes,
        List<Capture> altCapture,
        @Nullable Double netDamage,
        Map<String, Object> raw
) {
    public enum Kind {CRITTER, MOB}

    public enum Capture {LASSO, FISHING_NET, BLACK_HOLE, CHARM, SPECIAL, NONE}

    public enum Medium {LAND, WATER, AIR}

    public enum Behavior {WANDER, HOP, AGGRESSIVE, STATIONARY, FLY}

    public enum NetTier {
        BASIC(5, 8), MEDIUM(25, 12), TURBO(50, 16), GIGANTIC(100, 20);

        private final double pull;
        private final double range;

        NetTier(double pull, double range) {
            this.pull = pull;
            this.range = range;
        }

        public double pull() {
            return pull;
        }

        public double range() {
            return range;
        }
    }

    public record Xp(double hunting, double combat, double farming, double fishing) {
    }

    public record Drop(String item, int min, int max, double chance, @Nullable String enchant, int level) {
    }

    public record Bestiary(String section, String bracket, int maxTier, String lore, @Nullable String family) {
        public int bracketNumber() {
            return switch (bracket) {
                case "HUNTING_1", "HUNTING_2", "HUNTING_3", "HUNTING_4", "HUNTING_5" -> 8 + Integer.parseInt(bracket.substring(8));
                default -> Integer.parseInt(bracket);
            };
        }
    }

    public record Spawn(List<RegionType> regions, List<Pos> points, int max, @Nullable Trigger trigger) {
    }

    public record Trigger(String type, double chance, Map<String, Object> details) {
    }

    public record Message(String when, String text) {
    }

    public boolean isCritter() {
        return kind == Kind.CRITTER;
    }

    public boolean hasSpecial(String id) {
        return id.equals(special);
    }

    @SuppressWarnings("unchecked")
    static CreatureSpec parse(Map<String, Object> raw) {
        Map<String, Object> xp = map(raw.get("xp"));
        Map<String, Object> bestiary = map(raw.get("bestiary"));
        Map<String, Object> spawn = map(raw.get("spawn"));
        List<Drop> drops = new ArrayList<>();
        for (Object row : list(raw.get("drops"))) {
            Map<String, Object> drop = (Map<String, Object>) row;
            int min = integer(drop.get("min"), 1);
            drops.add(new Drop(text(drop.get("item")), min, integer(drop.get("max"), min), number(drop.get("chance"), 100),
                    drop.get("enchant") == null ? null : text(drop.get("enchant")), integer(drop.get("level"), 1)));
        }
        List<RegionType> regions = new ArrayList<>();
        for (Object region : list(spawn.get("regions"))) regions.add(RegionType.valueOf(text(region)));
        List<Pos> points = new ArrayList<>();
        for (Object point : list(spawn.get("points"))) {
            List<Object> xyz = (List<Object>) point;
            points.add(new Pos(number(xyz.get(0), 0) + 0.5, number(xyz.get(1), 0), number(xyz.get(2), 0) + 0.5));
        }
        List<Message> messages = new ArrayList<>();
        for (Object row : list(raw.get("messages"))) {
            Map<String, Object> message = (Map<String, Object>) row;
            messages.add(new Message(text(message.get("when")), text(message.get("text"))));
        }
        List<MobType> types = new ArrayList<>();
        for (Object type : list(raw.get("mobTypes"))) types.add(MobType.valueOf(text(type)));
        String entityName = raw.containsKey("entity") ? text(raw.get("entity")) : "ZOMBIE";
        EntityType entity = EntityType.fromKey("minecraft:" + entityName.toLowerCase());
        if (entity == null) throw new IllegalArgumentException("Unknown entity " + entityName + " for " + raw.get("id"));
        return new CreatureSpec(
                text(raw.get("id")), text(raw.get("name")), text(raw.get("island")),
                Kind.valueOf(text(raw.getOrDefault("kind", "CRITTER"))),
                integer(raw.get("level"), 1), number(raw.get("health"), 100), number(raw.get("damage"), 0),
                number(raw.get("speed"), 100), entity, List.copyOf(types),
                raw.get("shard") == null ? null : text(raw.get("shard")),
                Capture.valueOf(text(raw.getOrDefault("capture", "NONE"))),
                integer(raw.get("staminaBars"), 1),
                NetTier.valueOf(text(raw.getOrDefault("minNet", "BASIC"))),
                raw.get("special") == null ? null : text(raw.get("special")),
                Medium.valueOf(text(raw.getOrDefault("medium", "LAND"))),
                Behavior.valueOf(text(raw.getOrDefault("behavior", "WANDER"))),
                new Xp(number(xp.get("hunting"), 0), number(xp.get("combat"), 0), number(xp.get("farming"), 0), number(xp.get("fishing"), 0)),
                integer(raw.get("orbs"), 0), integer(raw.get("coins"), 0), List.copyOf(drops),
                number(raw.get("shardChance"), 100),
                new Bestiary(text(bestiary.getOrDefault("section", "MOONGLADE_MARSH")), text(bestiary.getOrDefault("bracket", "HUNTING_1")),
                        integer(bestiary.get("maxTier"), 10), text(bestiary.getOrDefault("lore", "")),
                        bestiary.get("family") == null ? null : text(bestiary.get("family"))),
                new Spawn(List.copyOf(regions), List.copyOf(points), integer(spawn.get("max"), 4), trigger(spawn.get("trigger"))),
                List.copyOf(messages), text(raw.getOrDefault("notes", "")), alternatives(raw.get("altCapture")),
                raw.get("netDamage") instanceof Number netDamage ? netDamage.doubleValue() : null, java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(raw)));
    }

    private static List<Capture> alternatives(Object value) {
        List<Capture> result = new ArrayList<>();
        if (value instanceof List<?> rows) rows.forEach(row -> result.add(Capture.valueOf(text(row))));
        else if (value != null) result.add(Capture.valueOf(text(value)));
        return List.copyOf(result);
    }

    private static @Nullable Trigger trigger(Object value) {
        Map<String, Object> raw = map(value);
        if (raw.isEmpty()) return null;
        return new Trigger(text(raw.getOrDefault("type", "")), number(raw.get("chance"), 100), java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(raw)));
    }

    public boolean hasOwnBestiary() {
        return raw.get("bestiary") instanceof Map<?, ?> map && map.containsKey("bracket");
    }

    public CreatureSpec withBestiary(Bestiary replacement) {
        return new CreatureSpec(id, name, island, kind, level, health, damage, speed, entity, mobTypes, shard, capture, staminaBars,
                minNet, special, medium, behavior, xp, orbs, coins, drops, shardChance, replacement, spawn, messages, notes,
                altCapture, netDamage, raw);
    }

    public boolean captures(Capture method) {
        return capture == method || altCapture.contains(method);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private static List<Object> list(Object value) {
        return value instanceof List<?> list ? new ArrayList<>(list) : List.of();
    }

    private static String text(Object value) {
        return String.valueOf(value);
    }

    private static int integer(Object value, int fallback) {
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static double number(Object value, double fallback) {
        return value instanceof Number number ? number.doubleValue() : fallback;
    }
}
