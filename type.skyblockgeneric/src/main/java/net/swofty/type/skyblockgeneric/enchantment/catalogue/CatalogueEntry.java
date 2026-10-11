package net.swofty.type.skyblockgeneric.enchantment.catalogue;

import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// One enchantment's real-Hypixel reference data: per-level text, sources, applicable items, conflicts.
public record CatalogueEntry(String key, String name, boolean ultimate, int maxLevel, Map<Integer, String> levels,
                             List<String> sources, List<String> appliedTo, List<String> conflicts, List<Integer> costs) {

    public String description(int level) {
        TreeMap<Integer, String> sorted = new TreeMap<>(levels);
        var exact = sorted.get(level);
        if (exact != null) return exact;
        var above = sorted.ceilingEntry(level);
        return above != null ? above.getValue() : sorted.lastEntry().getValue();
    }

    public int minLevel() {
        return levels.keySet().stream().min(Integer::compareTo).orElse(1);
    }

    public int cost(int level) {
        if (costs.isEmpty()) return level * 10;
        return costs.get(Math.min(level, costs.size()) - 1);
    }

    public List<EnchantItemGroups> groups() {
        return appliedTo.stream().map(EnchantItemGroups::fromLabel).flatMap(java.util.Optional::stream).distinct().toList();
    }
}
