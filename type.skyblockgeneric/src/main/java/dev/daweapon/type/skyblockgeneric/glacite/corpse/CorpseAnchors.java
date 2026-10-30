package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CorpseAnchors {
    private static final Map<String, List<Pos>> LAYOUTS = load();

    private CorpseAnchors() {
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<Pos>> load() {
        Map<String, List<Pos>> layouts = new LinkedHashMap<>();
        Map<String, List<List<Number>>> raw = (Map<String, List<List<Number>>>) CorpseFiles.read("anchors.yml").get("layouts");
        raw.forEach((code, spots) -> {
            List<Pos> positions = new ArrayList<>();
            for (List<Number> spot : spots) {
                positions.add(new Pos(spot.get(0).intValue() + 0.5, spot.get(1).intValue(), spot.get(2).intValue() + 0.5));
            }
            layouts.put(code, List.copyOf(positions));
        });
        return layouts;
    }

    public static List<Pos> spots(String layoutCode) {
        return LAYOUTS.getOrDefault(layoutCode, List.of());
    }

    public static List<String> layoutCodes() {
        return List.copyOf(LAYOUTS.keySet());
    }

    public static List<Pos> shuffled(String layoutCode) {
        List<Pos> spots = new ArrayList<>(spots(layoutCode));
        Collections.shuffle(spots);
        return spots;
    }
}
