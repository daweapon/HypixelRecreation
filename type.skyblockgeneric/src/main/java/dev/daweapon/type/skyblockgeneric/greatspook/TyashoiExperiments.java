package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.YamlFileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TyashoiExperiments {
    private static final File FILE = new File("./configuration/skyblock/great_spook_experiments.yml");
    private static volatile Map<String, TyashoiExperiment> experiments;

    private TyashoiExperiments() {
    }

    public static synchronized Map<String, TyashoiExperiment> all() {
        if (experiments == null) experiments = parse(FILE);
        return experiments;
    }

    public static @Nullable TyashoiExperiment get(String id) {
        return all().get(id);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, TyashoiExperiment> parse(File file) {
        Map<String, TyashoiExperiment> loaded = new LinkedHashMap<>();
        List<Map<String, Object>> raw;
        try {
            raw = (List<Map<String, Object>>) YamlFileUtils.loadYaml(file).get("experiments");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        raw.forEach(entry -> {
            TyashoiExperiment experiment = TyashoiExperiment.parse(entry);
            loaded.put(experiment.id(), experiment);
        });
        return loaded;
    }
}
