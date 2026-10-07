package net.swofty.type.skyblockgeneric.pettraining;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minestom.server.item.Material;

@Getter
@RequiredArgsConstructor
public enum PetTrainingType {
    FREE("Free", "<a>", Material.WOODEN_SWORD, 10, 0, 0, 0),
    LIGHT("Light", "<a>", Material.STONE_SWORD, 20, 200_000, 0, 250_000),
    MODERATE("Moderate", "<9>", Material.IRON_SWORD, 30, 1_000_000, 0, 500_000),
    EXPERT("Expert", "<5>", Material.GOLDEN_SWORD, 40, 2_000_000, 0, 750_000),
    ULTRA("Ultra", "<6>", Material.DIAMOND_SWORD, 50, 2_750_000, 0, 1_500_000),
    TURBO("Turbo!", "<d>", Material.NETHERITE_SWORD, 60, 3_000_000, 500, 2_500_000),
    ;

    private static final double FREE_XP_AT_TAMING_10 = 20_000;
    private static final double FREE_XP_PER_EXTRA_TAMING_LEVEL = 600;
    private static final double FREE_XP_CAP = 50_000;

    private final String displayName;
    private final String color;
    private final Material icon;
    private final int requiredTamingLevel;
    private final double coinsPerDay;
    private final int bitsPerDay;
    private final double fixedXpPerDay;

    public double xpPerDay(int tamingLevel) {
        if (this != FREE) return fixedXpPerDay;
        double scaled = FREE_XP_AT_TAMING_10 + Math.max(0, tamingLevel - FREE.requiredTamingLevel) * FREE_XP_PER_EXTRA_TAMING_LEVEL;
        return Math.min(FREE_XP_CAP, scaled);
    }

    public boolean isUnlocked(int tamingLevel) {
        return tamingLevel >= requiredTamingLevel;
    }
}
