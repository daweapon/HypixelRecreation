package dev.daweapon.type.skyblockgeneric.rabbits;

import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChocolateFactoryLevel {
    ONE(RabbitRarity.UNCOMMON, "Uncommon", 6),
    TWO(RabbitRarity.RARE, "Rare", 12),
    THREE(RabbitRarity.EPIC, "Epic", 18),
    FOUR(RabbitRarity.LEGENDARY, "Legendary", 22),
    FIVE(RabbitRarity.MYTHIC, "Mythic", 25),
    SIX(RabbitRarity.DIVINE, "Special", 28);

    private final RabbitRarity maxRarity;
    private final String maxRarityName;
    private final int maxHitmanSlots;

    public static ChocolateFactoryLevel forPrestige(int prestigeLevel) {
        return values()[Math.max(0, Math.min(prestigeLevel, values().length - 1))];
    }
}
