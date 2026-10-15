package dev.daweapon.type.skyblockgeneric.fishing.resolver;

import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.fishing.FishingContext;
import dev.daweapon.type.skyblockgeneric.fishing.catches.CatchPayload;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingTableDefinition;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingTableDefinition.TreasureQuality;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingTableDefinition.WeightedLoot;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hypixel's island treasure pools (wiki: Treasure): a cast that caught nothing else rolls Treasure Chance; a
 * treasure is GOOD, GREAT or OUTSTANDING by share, with the better qualities boosted additively by Blessing (+2%
 * per level) and quality bait; otherwise a normal catch is picked by weight. Entries for items this server doesn't
 * have yet are skipped. Fishing XP for these catches isn't on the wiki; 10 (normal) and 15 (treasure) are estimates.
 */
final class QualityTreasureRoller {
    static final double NORMAL_XP = 10D;
    static final double TREASURE_XP = 15D;

    private QualityTreasureRoller() {
    }

    static CatchPayload roll(FishingContext context, FishingTableDefinition table) {
        UUID uuid = context.player().getUuid();
        CatchPayload forced = forced(uuid, table);
        if (forced != null) return forced;

        double treasureChance = FishingLootResolver.playerStatistic(context, ItemStatistic.TREASURE_CHANCE);
        if (context.bait() != null) treasureChance += context.bait().getTreasureChanceBonus();
        if (LootRoll.roll(uuid, null, treasureChance)) {
            TreasureQuality quality = pickQuality(uuid, table.treasureQualities(), qualityBonus(context));
            if (rollTreasureDye(uuid, quality)) return treasureDye(quality);
            List<WeightedLoot> pool = available(quality.entries());
            if (!pool.isEmpty()) {
                WeightedLoot loot = LootRoll.pick(uuid, pool, QualityTreasureRoller::itemOf, WeightedLoot::weight, ThreadLocalRandom.current());
                return new CatchPayload.Treasure(loot.itemId(), amount(loot), TREASURE_XP, quality.name());
            }
        }
        List<WeightedLoot> normal = available(table.weightedItems());
        WeightedLoot loot = LootRoll.pick(uuid, normal, QualityTreasureRoller::itemOf, WeightedLoot::weight, ThreadLocalRandom.current());
        return new CatchPayload.Item(loot.itemId(), amount(loot), NORMAL_XP, false);
    }

    static Optional<CatchPayload> treasure(FishingContext context, FishingTableDefinition table) {
        UUID uuid = context.player().getUuid();
        TreasureQuality quality = pickQuality(uuid, table.treasureQualities(), qualityBonus(context));
        if (rollTreasureDye(uuid, quality)) return Optional.of(treasureDye(quality));
        List<WeightedLoot> pool = available(quality.entries());
        if (pool.isEmpty()) return Optional.empty();
        WeightedLoot loot = LootRoll.pick(uuid, pool, QualityTreasureRoller::itemOf, WeightedLoot::weight, ThreadLocalRandom.current());
        return Optional.of(new CatchPayload.Treasure(loot.itemId(), amount(loot), TREASURE_XP, quality.name()));
    }

    private static boolean rollTreasureDye(UUID uuid, TreasureQuality quality) {
        double chance = switch (quality.name()) {
            case "GOOD" -> 1D / 100_000 * 100;
            case "GREAT" -> 1D / 10_000 * 100;
            case "OUTSTANDING" -> 1D / 1_000 * 100;
            default -> 0;
        };
        return LootRoll.roll(uuid, ItemType.DYE_TREASURE, chance);
    }

    private static CatchPayload treasureDye(TreasureQuality quality) {
        return new CatchPayload.Treasure(ItemType.DYE_TREASURE.name(), 1, TREASURE_XP, quality.name());
    }

    private static CatchPayload forced(UUID uuid, FishingTableDefinition table) {
        for (TreasureQuality quality : table.treasureQualities()) {
            for (WeightedLoot loot : available(quality.entries())) {
                ItemType type = itemOf(loot);
                if (type != null && LootRoll.claim(uuid, List.of(type)) != null) {
                    return new CatchPayload.Treasure(loot.itemId(), amount(loot), TREASURE_XP, quality.name());
                }
            }
        }
        return null;
    }

    static double qualityBonus(FishingContext context) {
        double bonus = context.bait() == null ? 0 : context.bait().getTreasureQualityBonus();
        var blessing = context.rod().getAttributeHandler().getEnchantment(EnchantmentType.BLESSING);
        if (blessing != null) bonus += blessing.level() * 2D;
        return bonus + PetAbilities.value(context.player(), ItemType.HERMIT_CRAB_PET, "Seafloor Scalper", 0);
    }

    static TreasureQuality pickQuality(UUID uuid, List<TreasureQuality> qualities, double bonusPercent) {
        List<TreasureQuality> boosted = new ArrayList<>();
        for (TreasureQuality quality : qualities) {
            boolean better = !"GOOD".equals(quality.name());
            double share = better ? quality.share() * (1 + bonusPercent / 100D) : quality.share();
            boosted.add(new TreasureQuality(quality.name(), share, quality.entries()));
        }
        return LootRoll.pick(uuid, boosted, quality -> null, TreasureQuality::share, ThreadLocalRandom.current());
    }

    private static List<WeightedLoot> available(List<WeightedLoot> entries) {
        return entries.stream()
                .filter(loot -> FishingTableDefinition.COINS.equals(loot.itemId()) || ItemType.get(loot.itemId()) != null)
                .toList();
    }

    private static ItemType itemOf(WeightedLoot loot) {
        return ItemType.get(loot.itemId());
    }

    private static int amount(WeightedLoot loot) {
        return loot.min() >= loot.max() ? loot.min() : ThreadLocalRandom.current().nextInt(loot.min(), loot.max() + 1);
    }
}
