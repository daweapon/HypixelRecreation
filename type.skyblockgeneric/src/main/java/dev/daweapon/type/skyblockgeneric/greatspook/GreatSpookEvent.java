package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.skyblockgeneric.utility.SharedOverride;
import org.tinylog.Logger;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

public final class GreatSpookEvent {
    public static final String NAME = "Great Spook";
    private static final File FILE = new File("./configuration/skyblock/great_spook.yml");
    private static final SharedOverride OVERRIDE = new SharedOverride("hsb:greatspook:override");

    private static volatile List<Window> windows;

    private GreatSpookEvent() {
    }

    public record Window(LocalDate start, LocalDate end) {
        boolean contains(LocalDate date) {
            return !date.isBefore(start) && !date.isAfter(end);
        }
    }

    public static void initialize() {
        OVERRIDE.value();
    }

    public static boolean isActive() {
        Boolean override = OVERRIDE.value();
        if (override != null) return override;
        return configuredWindows().stream().anyMatch(window -> window.contains(LocalDate.now(ZoneOffset.UTC)));
    }

    public static boolean setOverride(boolean active) {
        return OVERRIDE.set(active);
    }

    public static boolean clearOverride() {
        return OVERRIDE.clear();
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
