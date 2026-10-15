package dev.daweapon.type.skyblockgeneric.user.pets;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Pet Items were first added under made-up ids (e.g. {@code ALL_SKILLS_EXP_BOOST}); Hypixel's ids are
 * {@code PET_ITEM_*}. Rewrites an old saved id, whether on an item or held by a pet, to its Hypixel id.
 */
public final class PetItemLegacyIds {
    public static final Map<String, String> RENAMED = Map.ofEntries(
            Map.entry("ALL_SKILLS_EXP_BOOST", "PET_ITEM_ALL_SKILLS_BOOST_COMMON"),
            Map.entry("BIG_TEETH", "PET_ITEM_BIG_TEETH_COMMON"),
            Map.entry("BUBBLEGUM", "PET_ITEM_BUBBLEGUM"),
            Map.entry("COMBAT_EXP_BOOST", "PET_ITEM_COMBAT_SKILL_BOOST_COMMON"),
            Map.entry("FARMING_EXP_BOOST_COMMON", "PET_ITEM_FARMING_SKILL_BOOST_COMMON"),
            Map.entry("FARMING_EXP_BOOST_RARE", "PET_ITEM_FARMING_SKILL_BOOST_RARE"),
            Map.entry("FISHING_EXP_BOOST", "PET_ITEM_FISHING_SKILL_BOOST_COMMON"),
            Map.entry("FORAGING_EXP_BOOST", "PET_ITEM_FORAGING_SKILL_BOOST_COMMON"),
            Map.entry("HARDENED_SCALES", "PET_ITEM_HARDENED_SCALES_UNCOMMON"),
            Map.entry("IRON_CLAWS", "PET_ITEM_IRON_CLAWS_COMMON"),
            Map.entry("MINING_EXP_BOOST_COMMON", "PET_ITEM_MINING_SKILL_BOOST_COMMON"),
            Map.entry("MINING_EXP_BOOST_RARE", "PET_ITEM_MINING_SKILL_BOOST_RARE"),
            Map.entry("SHARPENED_CLAWS", "PET_ITEM_SHARPENED_CLAWS_UNCOMMON"));

    private PetItemLegacyIds() {
    }

    public static @Nullable String migrate(@Nullable String savedId) {
        return savedId == null ? null : RENAMED.getOrDefault(savedId, savedId);
    }
}
