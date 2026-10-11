package net.swofty.type.skyblockgeneric.enchantment;

import net.swofty.type.skyblockgeneric.enchantment.abstr.ConflictingEnch;
import net.swofty.type.skyblockgeneric.enchantment.catalogue.CatalogueEntry;
import net.swofty.type.skyblockgeneric.enchantment.catalogue.EnchantmentCatalogue;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

// Which enchantments exclude each other: the catalogue's conflict lists (made symmetric) plus any ConflictingEnch,
// the single-Ultimate rule, and One For All wiping everything else.
public final class EnchantmentConflicts {
    private static final Map<EnchantmentType, Set<EnchantmentType>> CONFLICTS = build();

    private EnchantmentConflicts() {}

    public static Set<EnchantmentType> of(EnchantmentType type) {
        return CONFLICTS.getOrDefault(type, Set.of());
    }

    public static void applyTo(SkyBlockItem item, EnchantmentType incoming) {
        for (EnchantmentType existing : EnchantmentType.values()) {
            if (existing == incoming || !item.getAttributeHandler().hasEnchantment(existing)) continue;
            if (excludes(incoming, existing)) item.getAttributeHandler().removeEnchantment(existing);
        }
    }

    public static boolean blockedBy(SkyBlockItem item, EnchantmentType incoming) {
        if (incoming == EnchantmentType.ONE_FOR_ALL) return true;
        for (EnchantmentType existing : EnchantmentType.values()) {
            if (existing == incoming || !item.getAttributeHandler().hasEnchantment(existing)) continue;
            if (excludes(incoming, existing)) return true;
        }
        return false;
    }

    public static boolean excludes(EnchantmentType incoming, EnchantmentType existing) {
        if (incoming == EnchantmentType.ONE_FOR_ALL) return true;
        if (incoming.getEnch().isUltimate() && existing.getEnch().isUltimate()) return true;
        return of(incoming).contains(existing);
    }

    private static Map<EnchantmentType, Set<EnchantmentType>> build() {
        Map<String, EnchantmentType> byName = new java.util.HashMap<>();
        for (EnchantmentType type : EnchantmentType.values()) byName.put(type.getName(), type);

        Map<EnchantmentType, Set<EnchantmentType>> conflicts = new EnumMap<>(EnchantmentType.class);
        for (EnchantmentType type : EnchantmentType.values()) {
            if (type.getEnch() instanceof ConflictingEnch conflicting)
                conflicting.getConflictingEnchantments().forEach(other -> link(conflicts, type, other));
            CatalogueEntry entry = EnchantmentCatalogue.find(type.name());
            if (entry == null) continue;
            for (String name : entry.conflicts()) {
                EnchantmentType other = byName.get(name);
                if (other != null) link(conflicts, type, other);
            }
        }
        return conflicts;
    }

    private static void link(Map<EnchantmentType, Set<EnchantmentType>> conflicts, EnchantmentType a, EnchantmentType b) {
        conflicts.computeIfAbsent(a, _ -> EnumSet.noneOf(EnchantmentType.class)).add(b);
        conflicts.computeIfAbsent(b, _ -> EnumSet.noneOf(EnchantmentType.class)).add(a);
    }
}
