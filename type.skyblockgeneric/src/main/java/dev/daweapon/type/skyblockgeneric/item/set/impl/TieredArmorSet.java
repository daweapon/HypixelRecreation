package dev.daweapon.type.skyblockgeneric.item.set.impl;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.set.ArmorSetRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * A set whose bonus starts below the full set and scales with the number of pieces worn,
 * shown on Hypixel as a "Tiered Bonus".
 */
public interface TieredArmorSet extends ArmorSet {
    int minimumPieces();

    List<String> getDescription(int wornPieces);

    default ItemStatistics getStatistics(SkyBlockPlayer player, int wornPieces) {
        return ItemStatistics.empty();
    }

    @Override
    default ArrayList<String> getDescription() {
        return new ArrayList<>(getDescription(minimumPieces()));
    }

    default boolean isActive(SkyBlockPlayer player) {
        return getWornPieceCount(player) >= minimumPieces();
    }

    static int tierIndex(int wornPieces, int minimumPieces) {
        return Math.max(0, Math.min(wornPieces, 4) - minimumPieces);
    }

    static ItemStatistics statistics(SkyBlockPlayer player) {
        ItemStatistics total = ItemStatistics.empty();
        for (ArmorSetRegistry registry : ArmorSetRegistry.values()) {
            if (registry.getClazz() == null || !TieredArmorSet.class.isAssignableFrom(registry.getClazz())) continue;
            try {
                TieredArmorSet set = (TieredArmorSet) registry.getClazz().getDeclaredConstructor().newInstance();
                int worn = set.getWornPieceCount(player);
                if (worn >= set.minimumPieces()) total = ItemStatistics.add(total, set.getStatistics(player, worn));
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return total;
    }
}
