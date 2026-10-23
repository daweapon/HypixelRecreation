package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.commons.YamlFileUtils;
import net.minestom.server.coordinate.Pos;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record ShrineConfig(long effectIntervalMillis, long effectWindowMillis, Pos shrine, Pos ancientPig, Pos technoshopPig,
                           double relicHoverRadius, double relicHoverHeight, List<RelicSpot> relics) {
    private static final File FILE = new File("./configuration/skyblock/technoblade/shrine.yml");
    private static volatile ShrineConfig loaded;

    public record RelicSpot(int index, Pos position, String hint) {
    }

    public static ShrineConfig get() {
        ShrineConfig config = loaded;
        if (config != null) return config;
        synchronized (ShrineConfig.class) {
            if (loaded == null) loaded = load();
            return loaded;
        }
    }

    @SuppressWarnings("unchecked")
    private static ShrineConfig load() {
        try {
            Map<String, Object> root = YamlFileUtils.loadYaml(FILE);
            List<RelicSpot> relics = new ArrayList<>();
            List<Map<String, Object>> rawRelics = (List<Map<String, Object>>) root.get("relics");
            for (int index = 0; index < rawRelics.size(); index++) {
                Map<String, Object> raw = rawRelics.get(index);
                relics.add(new RelicSpot(index, pos(raw), String.valueOf(raw.getOrDefault("hint", "somewhere in the Hub"))));
            }
            return new ShrineConfig(
                    number(root.get("effect_interval_minutes")) * 60_000L,
                    number(root.get("effect_window_hours")) * 3_600_000L,
                    pos((Map<String, Object>) root.get("shrine")),
                    pos((Map<String, Object>) root.get("ancient_pig")),
                    pos((Map<String, Object>) root.get("technoshop_pig")),
                    ((Number) root.get("relic_hover_radius")).doubleValue(),
                    ((Number) root.get("relic_hover_height")).doubleValue(),
                    List.copyOf(relics));
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load the Blood God Shrine configuration");
            return new ShrineConfig(20 * 60_000L, 48 * 3_600_000L, Pos.ZERO, Pos.ZERO, Pos.ZERO, 2.5, 74, List.of());
        }
    }

    private static long number(Object raw) {
        return ((Number) raw).longValue();
    }

    private static Pos pos(Map<String, Object> raw) {
        double yaw = raw.get("yaw") instanceof Number number ? number.doubleValue() : 0;
        return new Pos(((Number) raw.get("x")).doubleValue(), ((Number) raw.get("y")).doubleValue(),
                ((Number) raw.get("z")).doubleValue(), (float) yaw, 0);
    }

    public Pos relicHoverPosition(int index) {
        double angle = 2 * Math.PI * index / Math.max(1, relics.size());
        return new Pos(shrine.x() + Math.cos(angle) * relicHoverRadius, relicHoverHeight, shrine.z() + Math.sin(angle) * relicHoverRadius);
    }
}
