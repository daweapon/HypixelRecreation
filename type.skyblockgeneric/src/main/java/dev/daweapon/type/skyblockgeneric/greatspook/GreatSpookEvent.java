package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.data.SharedData;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

public final class GreatSpookEvent {
    public static final String NAME = "Great Spook";
    private static final File FILE = new File("./configuration/skyblock/great_spook.yml");
    private static final String OVERRIDE_KEY = "hsb:greatspook:override";
    private static final long REFRESH_MILLIS = 2_000;

    private static volatile Boolean override;
    private static volatile List<Window> windows;

    private GreatSpookEvent() {
    }

    public record Window(LocalDate start, LocalDate end) {
        boolean contains(LocalDate date) {
            return !date.isBefore(start) && !date.isAfter(end);
        }
    }

    public static void initialize() {
        Thread.startVirtualThread(() -> {
            while (true) {
                try {
                    Thread.sleep(REFRESH_MILLIS);
                } catch (InterruptedException interrupted) {
                    return;
                }
                refreshOverride();
            }
        });
    }

    public static boolean isActive() {
        if (override != null) return override;
        return configuredWindows().stream().anyMatch(window -> window.contains(LocalDate.now(ZoneOffset.UTC)));
    }

    public static boolean setOverride(boolean active) {
        if (SharedData.jedisPool() == null) return false;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.set(OVERRIDE_KEY, Boolean.toString(active));
        }
        override = active;
        return true;
    }

    public static boolean clearOverride() {
        if (SharedData.jedisPool() == null) return false;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.del(OVERRIDE_KEY);
        }
        override = null;
        return true;
    }

    private static void refreshOverride() {
        if (SharedData.jedisPool() == null) return;
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            String raw = jedis.get(OVERRIDE_KEY);
            override = raw == null ? null : Boolean.parseBoolean(raw);
        } catch (Exception exception) {
            Logger.error(exception, "Failed to read the Great Spook override");
        }
    }

    @SuppressWarnings("unchecked")
    private static synchronized List<Window> configuredWindows() {
        if (windows != null) return windows;
        try {
            List<Map<String, Object>> raw = (List<Map<String, Object>>) YamlFileUtils.loadYaml(FILE).get("windows");
            windows = raw == null ? List.of() : raw.stream()
                    .map(entry -> new Window(LocalDate.parse(String.valueOf(entry.get("start"))),
                            LocalDate.parse(String.valueOf(entry.get("end")))))
                    .toList();
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load the Great Spook windows");
            windows = List.of();
        }
        return windows;
    }
}
