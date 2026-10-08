package net.swofty.type.skyblockgeneric.fishing.trophy;

import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointTrophyFish;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.fishing.FishingContext;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.fishing.catches.TrophyTier;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingRegistry;
import net.swofty.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Hypixel's trophy roll (wiki: Trophy_Fish): only on the Crimson Isle in lava, only when the cast caught no
 * Sea Creature. Eligible fish roll rarest first (ties alphabetical) at {@code base × (1 + TC/100)}, TC capped at
 * 150; the first success is the catch, then its tier rolls Diamond, Gold, Silver, else Bronze.
 */
public final class TrophyFishRoller {
    public static final double TROPHY_CHANCE_CAP = 150D;
    public static final double FISHING_XP_PER_TROPHY = 300D;
    static final int GOLD_PITY = 100;
    static final int DIAMOND_PITY = 600;
    private static final double DIAMOND_CHANCE = 0.2D;
    private static final double GOLD_CHANCE = 2D;
    private static final double SILVER_CHANCE = 25D;

    private TrophyFishRoller() {
    }

    public static boolean trophyWaters(FishingContext context) {
        return context.medium() == FishingMedium.LAVA
                && HypixelConst.getTypeLoader() != null
                && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_CRIMSON_ISLE;
    }

    public static Optional<CatchPayload> roll(FishingContext context, double trophyChance) {
        if (!trophyWaters(context)) return Optional.empty();
        UUID uuid = context.player().getUuid();

        Optional<CatchPayload> forced = forced(uuid);
        if (forced.isPresent()) return forced;

        double multiplier = 1D + Math.min(TROPHY_CHANCE_CAP, Math.max(0, trophyChance)) / 100D;
        for (TrophyFishDefinition definition : eligible(context)) {
            double chance = baseChance(context, definition) * multiplier;
            if (LootRoll.roll(uuid, null, chance)) {
                return Optional.of(payload(definition, rollTier(context.player(), definition, charmBonus(context))));
            }
        }
        return Optional.empty();
    }

    public static CatchPayload.TrophyFish payload(TrophyFishDefinition definition, TrophyTier tier) {
        return new CatchPayload.TrophyFish(definition.id(), tier, definition.itemId(tier), FISHING_XP_PER_TROPHY);
    }

    static List<TrophyFishDefinition> eligible(FishingContext context) {
        List<TrophyFishDefinition> eligible = new ArrayList<>();
        for (TrophyFishDefinition definition : FishingRegistry.getTrophyFish()) {
            if (definition.specialGoldenFish() || !definition.conditionsMet(context)) continue;
            eligible.add(definition);
        }
        eligible.sort(Comparator.<TrophyFishDefinition>comparingDouble(definition -> baseChance(context, definition))
                .thenComparing(TrophyFishDefinition::displayName));
        return eligible;
    }

    static double baseChance(FishingContext context, TrophyFishDefinition definition) {
        return definition.manaScaled() ? context.player().getMaxMana() / 1000D : definition.catchChance();
    }

    private static Optional<CatchPayload> forced(UUID uuid) {
        for (TrophyFishDefinition definition : FishingRegistry.getTrophyFish()) {
            for (TrophyTier tier : TrophyTier.orderedDescending()) {
                ItemType type = definition.itemType(tier);
                if (type != null && LootRoll.claim(uuid, List.of(type)) != null) {
                    return Optional.of(payload(definition, tier));
                }
            }
        }
        return Optional.empty();
    }

    static double charmBonus(FishingContext context) {
        var charm = context.rod().getAttributeHandler().getEnchantment(EnchantmentType.CHARM);
        return charm == null ? 0D : charm.level() * 2D;
    }

    public static TrophyTier rollTier(SkyBlockPlayer player, TrophyFishDefinition definition, double charmPercent) {
        DatapointTrophyFish.FishProgress progress = player.getTrophyFishData().getProgress(definition.id());
        if (progress.getTotalCatches() + 1 >= DIAMOND_PITY && !progress.hasTier(TrophyTier.DIAMOND.name())) {
            return TrophyTier.DIAMOND;
        }
        if (progress.getTotalCatches() + 1 >= GOLD_PITY && !progress.hasTier(TrophyTier.GOLD.name())) {
            return TrophyTier.GOLD;
        }
        double charm = 1D + charmPercent / 100D;
        UUID uuid = player.getUuid();
        if (LootRoll.roll(uuid, definition.itemType(TrophyTier.DIAMOND), DIAMOND_CHANCE * charm)) return TrophyTier.DIAMOND;
        if (LootRoll.roll(uuid, definition.itemType(TrophyTier.GOLD), GOLD_CHANCE * charm)) return TrophyTier.GOLD;
        if (LootRoll.roll(uuid, definition.itemType(TrophyTier.SILVER), SILVER_CHANCE * charm)) return TrophyTier.SILVER;
        return TrophyTier.BRONZE;
    }
}
