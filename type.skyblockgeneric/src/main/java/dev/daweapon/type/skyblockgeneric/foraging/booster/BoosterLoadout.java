package dev.daweapon.type.skyblockgeneric.foraging.booster;

import java.util.EnumMap;
import java.util.Map;

public final class BoosterLoadout {
    public static final BoosterLoadout EMPTY = new BoosterLoadout(new EnumMap<>(BoosterKind.class));

    private final Map<BoosterKind, Integer> levels;

    private BoosterLoadout(Map<BoosterKind, Integer> levels) {
        this.levels = levels;
    }

    public static BoosterLoadout parse(String serialized) {
        Map<BoosterKind, Integer> levels = new EnumMap<>(BoosterKind.class);
        if (serialized == null || serialized.isBlank()) return new BoosterLoadout(levels);
        for (String part : serialized.split(";")) {
            String[] pair = part.split(":");
            if (pair.length != 2) continue;
            try {
                levels.put(BoosterKind.valueOf(pair[0]), Integer.parseInt(pair[1]));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return new BoosterLoadout(levels);
    }

    public String serialize() {
        StringBuilder builder = new StringBuilder();
        levels.forEach((kind, level) -> {
            if (!builder.isEmpty()) builder.append(';');
            builder.append(kind.name()).append(':').append(level);
        });
        return builder.toString();
    }

    public int level(BoosterKind kind) {
        return levels.getOrDefault(kind, 0);
    }

    public int count() {
        return levels.size();
    }

    public Map<BoosterKind, Integer> asMap() {
        return Map.copyOf(levels);
    }

    public BoosterLoadout with(BoosterKind kind, int level) {
        Map<BoosterKind, Integer> copy = new EnumMap<>(BoosterKind.class);
        copy.putAll(levels);
        copy.put(kind, level);
        return new BoosterLoadout(copy);
    }

    public boolean accepts(BoosterKind kind, int incomingLevel) {
        int current = level(kind);
        if (incomingLevel == BoosterKind.COMMON) return current == 0;
        return incomingLevel == BoosterKind.UNCOMMON && current == BoosterKind.COMMON;
    }
}
