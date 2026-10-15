package dev.daweapon.type.skyblockgeneric.museum;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MuseumCatalogue {
    private static final File FILE = new File("./configuration/skyblock/museum/catalogue.yml");
    private static volatile MuseumCatalogue instance;

    private final Map<String, MuseumEntry> entries;
    private final Map<MuseumCategory, List<MuseumEntry>> byCategory;
    private final Map<ItemType, MuseumEntry> byItem;
    private final Map<ItemType, List<MuseumEntry>> allByItem;
    private final Map<String, List<MuseumEntry>> children;

    private MuseumCatalogue(List<MuseumEntry> all) {
        this.entries = new LinkedHashMap<>();
        this.byCategory = new EnumMap<>(MuseumCategory.class);
        this.byItem = new HashMap<>();
        this.allByItem = new HashMap<>();
        this.children = new HashMap<>();
        for (MuseumCategory category : MuseumCategory.values()) byCategory.put(category, new ArrayList<>());

        for (MuseumEntry entry : all) {
            entries.put(entry.id(), entry);
            byCategory.get(entry.category()).add(entry);
            for (String itemId : entry.requiredItemIds()) register(itemId, entry);
            entry.mapped().forEach(itemId -> register(itemId, entry));
            if (entry.parent() != null) children.computeIfAbsent(entry.parent(), _ -> new ArrayList<>()).add(entry);
        }
    }

    private void register(String itemId, MuseumEntry entry) {
        ItemType type = ItemType.get(itemId);
        if (type == null) return;
        byItem.putIfAbsent(type, entry);
        List<MuseumEntry> owners = allByItem.computeIfAbsent(type, _ -> new ArrayList<>());
        if (!owners.contains(entry)) owners.add(entry);
    }

    public static MuseumCatalogue get() {
        MuseumCatalogue current = instance;
        if (current == null) {
            synchronized (MuseumCatalogue.class) {
                if (instance == null) instance = fromFile(FILE);
                current = instance;
            }
        }
        return current;
    }

    public List<MuseumEntry> all() {
        return List.copyOf(entries.values());
    }

    public List<MuseumEntry> progressEntries() {
        return entries.values().stream().filter(entry -> !entry.category().isSpecial()).toList();
    }

    public List<MuseumEntry> entries(MuseumCategory category) {
        return Collections.unmodifiableList(byCategory.get(category));
    }

    public Optional<MuseumEntry> entry(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    public Optional<MuseumEntry> entryFor(@Nullable ItemType type) {
        return type == null ? Optional.empty() : Optional.ofNullable(byItem.get(type));
    }

    public List<MuseumEntry> entriesFor(@Nullable ItemType type) {
        return type == null ? List.of() : Collections.unmodifiableList(allByItem.getOrDefault(type, List.of()));
    }

    public List<MuseumEntry> childrenOf(MuseumEntry entry) {
        return children.getOrDefault(entry.id(), List.of());
    }

    public Optional<MuseumEntry> upgradeOf(MuseumEntry entry) {
        return entry.parent() == null ? Optional.empty() : entry(entry.parent());
    }

    public int totalXp() {
        return progressEntries().stream().mapToInt(MuseumEntry::xp).sum();
    }

    static MuseumCatalogue fromFile(File file) {
        return new MuseumCatalogue(load(file));
    }

    @SuppressWarnings("unchecked")
    private static List<MuseumEntry> load(File file) {
        List<MuseumEntry> all = new ArrayList<>();
        try {
            Map<String, Object> root = YamlFileUtils.loadYaml(file);
            Map<String, Object> categories = (Map<String, Object>) root.getOrDefault("categories", Map.of());
            for (Map.Entry<String, Object> category : categories.entrySet()) {
                MuseumCategory museumCategory = MuseumCategory.valueOf(category.getKey());
                for (Map<String, Object> raw : (List<Map<String, Object>>) category.getValue()) {
                    all.add(parse(raw, museumCategory));
                }
            }
            for (Map<String, Object> raw : (List<Map<String, Object>>) root.getOrDefault("special", List.of())) {
                all.add(parse(raw, MuseumCategory.SPECIAL));
            }
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load the Museum catalogue from {}", file.getPath());
        }
        return all;
    }

    @SuppressWarnings("unchecked")
    private static MuseumEntry parse(Map<String, Object> raw, MuseumCategory category) {
        return new MuseumEntry(
                String.valueOf(raw.get("id")),
                String.valueOf(raw.get("name")),
                category,
                ((Number) raw.getOrDefault("xp", 0)).intValue(),
                raw.get("stage") == null ? null : String.valueOf(raw.get("stage")),
                raw.get("parent") == null ? null : String.valueOf(raw.get("parent")),
                List.copyOf((List<String>) raw.getOrDefault("mapped", List.of())),
                List.copyOf((List<String>) raw.getOrDefault("pieces", List.of()))
        );
    }
}
