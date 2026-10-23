package dev.daweapon.type.skyblockgeneric.advent;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.generic.user.categories.Rank;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AdventCalendar {
    public static final int DAYS = 25;
    private static final File FILE = new File("./configuration/skyblock/advent_calendar.yml");
    private static Map<Integer, Day> days;

    public record RankedItem(String item, Rank rank) {
    }

    public record RankedSpray(String spray, Rank rank) {
    }

    public record Day(int number, Map<String, Integer> gifts, int coins, List<RankedItem> items, List<RankedSpray> sprays, List<String> unbuilt) {
    }

    private AdventCalendar() {
    }

    public static synchronized Map<Integer, Day> days() {
        if (days == null) days = load(FILE);
        return days;
    }

    public static Day day(int number) {
        return days().get(number);
    }

    @SuppressWarnings("unchecked")
    public static Map<Integer, Day> load(File file) {
        try {
            Map<Object, Map<String, Object>> raw = (Map<Object, Map<String, Object>>) YamlFileUtils.loadYaml(file).get("days");
            Map<Integer, Day> loaded = new LinkedHashMap<>();
            raw.forEach((key, entry) -> {
                int number = ((Number) key).intValue();
                Map<String, Integer> gifts = new LinkedHashMap<>();
                ((Map<String, Object>) entry.getOrDefault("gifts", Map.of())).forEach((gift, amount) -> gifts.put(gift, ((Number) amount).intValue()));
                List<RankedItem> items = new ArrayList<>();
                for (Map<String, Object> item : (List<Map<String, Object>>) entry.getOrDefault("items", List.of())) {
                    items.add(new RankedItem(String.valueOf(item.get("item")), Rank.valueOf(String.valueOf(item.get("rank")))));
                }
                List<RankedSpray> sprays = new ArrayList<>();
                for (Map<String, Object> spray : (List<Map<String, Object>>) entry.getOrDefault("sprays", List.of())) {
                    sprays.add(new RankedSpray(String.valueOf(spray.get("spray")), Rank.valueOf(String.valueOf(spray.get("rank")))));
                }
                List<String> unbuilt = new ArrayList<>();
                for (Object name : (List<Object>) entry.getOrDefault("unbuilt", List.of())) unbuilt.add(String.valueOf(name));
                loaded.put(number, new Day(number, gifts, ((Number) entry.getOrDefault("coins", 0)).intValue(), items, sprays, unbuilt));
            });
            return loaded;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
