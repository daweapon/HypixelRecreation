package dev.daweapon.type.skyblockgeneric.slayer;

import java.util.Optional;
import dev.daweapon.commons.skyblock.item.ItemType;

public record SlayerTierDefinition(
    SlayerTier tier,
    int cost,
    int requiredCombatXp,
    int slayerXp,
    int bossLevel,
    double bossHealth,
    double bossDamage,
    double bossSpeed,
    int tokenDrops,
    Optional<ItemType> tokenItem,
    int requiredCombatLevel,
    int requiredSlayerLevel
) {
    public String displayName(SlayerType type) {
        return type.displayName() + " " + tier.numeral();
    }
}
