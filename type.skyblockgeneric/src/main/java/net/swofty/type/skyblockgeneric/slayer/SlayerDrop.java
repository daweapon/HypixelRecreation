package net.swofty.type.skyblockgeneric.slayer;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * One row of a slayer boss drop table. Each tier the row drops at carries its amount range and its
 * chance. An unavailable row is shown in menus but never rolled.
 */
public record SlayerDrop(
    @Nullable ItemType item,
    @Nullable String name,
    @Nullable EnchantmentType enchantment,
    int enchantmentLevel,
    Pool pool,
    int requiredLevel,
    long rngMeterXp,
    @Nullable String unavailableReason,
    Map<SlayerTier, TierOdds> tiers
) {
    public enum Pool { TOKEN, MAIN, EXTRA, SEPARATE, LOOT_SHARE }

    public record TierOdds(int minAmount, int maxAmount, double chancePercent) {
        public int rollAmount() {
            return minAmount >= maxAmount ? minAmount : ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1);
        }

        public String amountText() {
            return minAmount == maxAmount ? String.valueOf(minAmount) : minAmount + "-" + maxAmount;
        }
    }

    public @Nullable TierOdds odds(SlayerTier tier) {
        return tiers.get(tier);
    }

    public boolean dropsAt(SlayerTier tier) {
        return tiers.containsKey(tier);
    }

    public boolean available() {
        return unavailableReason == null && item != null;
    }

    public boolean rollable(SlayerTier tier, int slayerLevel) {
        return available() && pool != Pool.LOOT_SHARE && dropsAt(tier) && slayerLevel >= requiredLevel;
    }

    public boolean hasRngMeter() {
        return rngMeterXp > 0;
    }

    public String key() {
        if (item == null) return name == null ? "UNKNOWN" : name.replaceAll("<[^>]*>", "").toUpperCase().replaceAll("[^A-Z0-9]+", "_");
        return enchantment == null ? item.name() : item.name() + ":" + enchantment.name() + ":" + enchantmentLevel;
    }
}
