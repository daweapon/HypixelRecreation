package dev.daweapon.type.skyblockgeneric.foraging.booster;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public enum BoosterSlot {
    AXE(Set.of("JUNGLE_AXE", "SERIOUSLY_DAMAGED_AXE", "FIG_AXE", "DECENT_AXE", "FIGSTONE_AXE", "TREECAPITATOR_AXE",
            "STRANDED_TREECAPITATOR", "HELIX_CHOPPER")),
    ARMOR(Set.of("CANOPY_HELMET", "CANOPY_CHESTPLATE", "CANOPY_LEGGINGS", "CANOPY_BOOTS",
            "FIG_HELMET", "FIG_CHESTPLATE", "FIG_LEGGINGS", "FIG_BOOTS",
            "HELIX_ARMOR_HELMET", "HELIX_ARMOR_CHESTPLATE", "HELIX_ARMOR_LEGGINGS", "HELIX_ARMOR_BOOTS")),
    EQUIPMENT(Set.of("MANGROVE_LOCKET", "HONEYCOMB_NECKLACE", "SAFARI_BELT", "MANGROVE_VINE", "TORRHUS_BELT",
            "MANGROVE_GRIPPERS", "DAVIDS_CLOAK", "VEILSHROOM_BRACELET"));

    private final Set<String> items;

    BoosterSlot(Set<String> items) {
        this.items = items;
    }

    public Set<String> items() {
        return items;
    }

    public static @Nullable BoosterSlot of(@Nullable String itemId) {
        if (itemId == null) return null;
        for (BoosterSlot slot : values()) if (slot.items.contains(itemId)) return slot;
        return null;
    }
}
