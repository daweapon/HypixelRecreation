package dev.daweapon.type.skyblockgeneric.farmingcontest;

import lombok.Getter;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Every crop Jacob can pick for a Farming Contest, in the order Anita's menus list them.
 * {@code cropsPerTenthFortune} is the Personal Bests rate from the wiki's Anita page.
 */
@Getter
public enum ContestCrop {
    WHEAT("Wheat", Material.WHEAT, ItemStatistic.WHEAT_FORTUNE, 1_000, null, ItemType.WHEAT),
    CARROT("Carrot", Material.CARROT, ItemStatistic.CARROT_FORTUNE, 3_000, null, ItemType.CARROT),
    POTATO("Potato", Material.POTATO, ItemStatistic.POTATO_FORTUNE, 3_000, null, ItemType.POTATO),
    PUMPKIN("Pumpkin", Material.CARVED_PUMPKIN, ItemStatistic.PUMPKIN_FORTUNE, 1_000, null, ItemType.PUMPKIN),
    MELON("Melon Slice", Material.MELON_SLICE, ItemStatistic.MELON_FORTUNE, 5_000, null, ItemType.MELON_SLICE),
    MUSHROOM("Mushroom", Material.RED_MUSHROOM, ItemStatistic.MUSHROOM_FORTUNE, 1_000, null,
            ItemType.RED_MUSHROOM, ItemType.BROWN_MUSHROOM, ItemType.MUSHROOM),
    CACTUS("Cactus", Material.CACTUS, ItemStatistic.CACTUS_FORTUNE, 2_000, null, ItemType.CACTUS),
    SUGAR_CANE("Sugar Cane", Material.SUGAR_CANE, ItemStatistic.SUGAR_CANE_FORTUNE, 2_000, null, ItemType.SUGAR_CANE),
    NETHER_WART("Nether Wart", Material.NETHER_WART, ItemStatistic.NETHER_WART_FORTUNE, 3_000, null, ItemType.NETHER_WART),
    COCOA_BEANS("Cocoa Beans", Material.COCOA_BEANS, ItemStatistic.COCOA_BEANS_FORTUNE, 3_000, null, ItemType.COCOA_BEANS),
    SUNFLOWER("Sunflower", Material.SUNFLOWER, null, 2_000, ContestCrop.GARDEN_ONLY, ItemType.SUNFLOWER),
    MOONFLOWER("Moonflower", Material.BLUE_ORCHID, null, 2_000, ContestCrop.GARDEN_ONLY, ItemType.MOONFLOWER),
    WILD_ROSE("Wild Rose", Material.ROSE_BUSH, null, 2_000, ContestCrop.GARDEN_ONLY, ItemType.WILD_ROSE);

    private static final String GARDEN_ONLY = "Only grows in the Garden, which isn't in this server yet.";
    public static final double PERSONAL_BEST_FORTUNE_CAP = 100D;

    private final String displayName;
    private final Material icon;
    private final @Nullable ItemStatistic fortuneStatistic;
    private final int cropsPerTenthFortune;
    private final @Nullable String unavailableReason;
    private final Set<ItemType> countedItems;

    ContestCrop(String displayName, Material icon, @Nullable ItemStatistic fortuneStatistic,
                int cropsPerTenthFortune, @Nullable String unavailableReason, ItemType... countedItems) {
        this.displayName = displayName;
        this.icon = icon;
        this.fortuneStatistic = fortuneStatistic;
        this.cropsPerTenthFortune = cropsPerTenthFortune;
        this.unavailableReason = unavailableReason;
        this.countedItems = Set.of(countedItems);
    }

    public boolean isAvailable() {
        return unavailableReason == null;
    }

    public double personalBestFortune(long personalBest) {
        double fortune = personalBest / (cropsPerTenthFortune * 10D);
        return Math.min(PERSONAL_BEST_FORTUNE_CAP, Math.floor(fortune * 100D) / 100D);
    }

    public static Optional<ContestCrop> fromDrop(ItemType type) {
        return Arrays.stream(values()).filter(crop -> crop.countedItems.contains(type)).findFirst();
    }
}
