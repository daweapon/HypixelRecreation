package net.swofty.commons.skyblock.item.reforge;

import lombok.Getter;
import net.swofty.commons.skyblock.statistics.ItemStatistics;

import java.util.Set;
import java.util.function.BiFunction;

@Getter
// Loaded reforge definition: its identity, which item types it applies to, and the stat calculation it applies at a given level.
public class Reforge {
    private final String id;
    private final String name;
    private final String prefix;
    private final Set<ReforgeType> applicableTypes;
    private final Set<String> applicableItems;
    private final boolean stoneOnly;
    private final BiFunction<ItemStatistics, Integer, ItemStatistics> calculation;

    public Reforge(String id, String name, String prefix, Set<ReforgeType> applicableTypes,
                   BiFunction<ItemStatistics, Integer, ItemStatistics> calculation) {
        this(id, name, prefix, applicableTypes, false, calculation);
    }

    public Reforge(String id, String name, String prefix, Set<ReforgeType> applicableTypes, boolean stoneOnly,
                   BiFunction<ItemStatistics, Integer, ItemStatistics> calculation) {
        this(id, name, prefix, applicableTypes, Set.of(), stoneOnly, calculation);
    }

    public Reforge(String id, String name, String prefix, Set<ReforgeType> applicableTypes,
                   Set<String> applicableItems, boolean stoneOnly,
                   BiFunction<ItemStatistics, Integer, ItemStatistics> calculation) {
        this.applicableItems = applicableItems;
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.applicableTypes = applicableTypes;
        this.stoneOnly = stoneOnly;
        this.calculation = calculation;
    }

    public ItemStatistics getAfterCalculation(ItemStatistics statistics, Integer level) {
        return calculation.apply(statistics, level);
    }

    public boolean isApplicableTo(ReforgeType type) {
        return applicableTypes.contains(type);
    }

    public boolean isApplicableTo(ReforgeType type, String itemId) {
        if (!applicableItems.isEmpty()) return applicableItems.contains(itemId);
        return isApplicableTo(type);
    }

    @Override
    public String toString() {
        return "Reforge{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", applicableTypes=" + applicableTypes +
                '}';
    }
}
