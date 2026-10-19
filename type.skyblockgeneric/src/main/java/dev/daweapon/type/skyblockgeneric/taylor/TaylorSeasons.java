package dev.daweapon.type.skyblockgeneric.taylor;

import dev.daweapon.commons.YamlFileUtils;
import org.tinylog.Logger;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class TaylorSeasons {
    private static final File FILE = new File("./configuration/skyblock/taylor.yml");
    private static final Season FALLBACK = new Season("fall_2026", "Fall Bundle", "taylor_bundle_fall_2026", LocalDate.EPOCH);

    private TaylorSeasons() {
    }

    public record Season(String id, String label, String shop, LocalDate starts) {
    }

    public static Season current(LocalDate today) {
        return current(FILE, today);
    }

    @SuppressWarnings("unchecked")
    public static Season current(File file, LocalDate today) {
        try {
            Map<String, Object> root = YamlFileUtils.loadYaml(file);
            List<Season> seasons = new ArrayList<>();
            for (Map<String, Object> raw : (List<Map<String, Object>>) root.get("seasons")) {
                seasons.add(new Season(String.valueOf(raw.get("id")), String.valueOf(raw.get("label")),
                        String.valueOf(raw.get("shop")), LocalDate.parse(String.valueOf(raw.get("starts")))));
            }
            Object override = root.get("override");
            if (override != null && !String.valueOf(override).isBlank()) {
                return seasons.stream().filter(season -> season.id().equals(String.valueOf(override))).findFirst().orElse(FALLBACK);
            }
            return seasons.stream().filter(season -> !season.starts().isAfter(today))
                    .max(Comparator.comparing(Season::starts))
                    .orElseGet(() -> seasons.stream().min(Comparator.comparing(Season::starts)).orElse(FALLBACK));
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load Taylor seasons");
            return FALLBACK;
        }
    }
}
