package dev.daweapon.type.skyblockgeneric.firesale;

import dev.daweapon.commons.YamlFileUtils;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FireSaleCatalogue {
    private static final File FILE = new File("./configuration/skyblock/firesales/fire_sales.yml");
    private static volatile Map<String, FireSale> sales;

    private FireSaleCatalogue() {
    }

    public static synchronized Map<String, FireSale> all() {
        if (sales != null) return sales;
        sales = parse(FILE);
        return sales;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, FireSale> parse(File file) {
        Map<String, FireSale> loaded = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> raw = (List<Map<String, Object>>) YamlFileUtils.loadYaml(file).get("sales");
            raw.forEach(entry -> {
                FireSale sale = FireSale.parse(entry);
                loaded.put(sale.id(), sale);
            });
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load Fire Sales");
        }
        return loaded;
    }

    public static @Nullable FireSale get(String id) {
        return all().get(id);
    }

    public static @Nullable FireSale next(@Nullable String afterId) {
        List<FireSale> list = List.copyOf(all().values());
        if (list.isEmpty()) return null;
        if (afterId == null) return list.getFirst();
        for (int index = 0; index < list.size(); index++) {
            if (list.get(index).id().equals(afterId)) return list.get((index + 1) % list.size());
        }
        return list.getFirst();
    }
}
