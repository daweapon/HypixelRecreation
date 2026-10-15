package dev.daweapon.type.skyblockgeneric.mythological;

import dev.daweapon.commons.skyblock.item.Rarity;

import java.util.List;
import java.util.Map;

/**
 * The stats of a Mythological creature when dug up with a Griffin of one rarity. The Griffin's
 * rarity picks both the variant and its name prefix.
 */
public record MythologicalVariant(Rarity griffinRarity, String mobId, int level, double health, double damage,
                                  int coins, int combatXp, int orbs, List<MythologicalDrop> drops) {
    private static final Map<Rarity, String> PREFIXES = Map.of(
            Rarity.UNCOMMON, "Blessed",
            Rarity.RARE, "Stalwart",
            Rarity.EPIC, "Venerable",
            Rarity.LEGENDARY, "Exalted",
            Rarity.MYTHIC, "Empyrean");

    public int displayLevel() {
        return Integer.parseInt(mobId.substring(mobId.lastIndexOf('_') + 1));
    }

    public String nameOf(String baseName) {
        String prefix = PREFIXES.get(griffinRarity);
        return prefix == null ? baseName : prefix + " " + baseName;
    }
}
