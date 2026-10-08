package net.swofty.type.skyblockgeneric.fishing.resolver;

import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.essence.EssencePerks;
import net.swofty.type.skyblockgeneric.essence.IceEssencePerk;
import net.swofty.type.skyblockgeneric.fishing.tag.RarityTag;
import net.swofty.type.skyblockgeneric.fishing.tag.TimeOfDayTag;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.skyblockgeneric.fishing.FishingContext;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.fishing.festival.FishingFestival;
import net.swofty.type.skyblockgeneric.fishing.item.FishingItemSupport;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingRegistry;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingTableDefinition;
import net.swofty.type.skyblockgeneric.fishing.registry.SeaCreatureDefinition;
import net.swofty.type.skyblockgeneric.fishing.lava.PeaceTreaty;
import net.swofty.type.skyblockgeneric.fishing.trophy.TrophyFishRoller;
import net.swofty.type.skyblockgeneric.fishing.rod.FishingRodPartService;
import net.swofty.type.skyblockgeneric.fishing.tag.FishingTag;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
// Rolls what a fishing cast actually catches: quest items, sea creatures, trophy fish, or a
// regular/treasure/junk item from the matching loot table, in priority order.
public final class FishingLootResolver {

    private static final CatchPayload DEFAULT_CATCH =
        new CatchPayload.Item("RAW_FISH", 1, 5.0D, false);

    public static CatchPayload resolve(FishingContext context) {
        Optional<CatchPayload> questCatch = tryResolveQuestCatch(context);
        if (questCatch.isPresent()) {
            return questCatch.get();
        }

        Optional<CatchPayload> seaCreature = tryResolveSeaCreature(context);
        if (seaCreature.isPresent()) {
            return seaCreature.get();
        }
        Optional<CatchPayload> trophyFish = TrophyFishRoller.roll(context, trophyChance(context));
        if (trophyFish.isPresent()) {
            return trophyFish.get();
        }
        return resolveItem(context);
    }

    public static double trophyChance(FishingContext context) {
        double total = playerStatistic(context, ItemStatistic.TROPHY_FISH_CHANCE);
        if (context.bait() != null) {
            total += context.bait().getTrophyFishChanceBonus();
        }
        return total;
    }

    private static Optional<CatchPayload> tryResolveQuestCatch(FishingContext context) {
        if (context.medium() != FishingMedium.WATER) return Optional.empty();
        if (context.regionId() == null) return Optional.empty();
        if (!"FISHING_OUTPOST".equals(context.regionId()) && !"FISHERMANS_HUT".equals(context.regionId())) {
            return Optional.empty();
        }
        if (!context.player().getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FISHERWOMAN_ENID)) {
            return Optional.empty();
        }
        if (context.player().getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_UNLOCKED_SHIP)) {
            return Optional.empty();
        }
        if (context.player().getShipState().getEngine() != null) return Optional.empty();
        if (context.player().countItem(ItemType.RUSTY_SHIP_ENGINE) > 0) return Optional.empty();
        if (!LootRoll.roll(context.player().getUuid(), ItemType.RUSTY_SHIP_ENGINE, 1.0D)) return Optional.empty();

        return Optional.of(new CatchPayload.Quest(
            ItemType.RUSTY_SHIP_ENGINE.name(),
            1,
            15.0D,
            "You fished up a Rusty Ship Engine!"
        ));
    }

    private static Optional<CatchPayload> tryResolveSeaCreature(FishingContext context) {
        Optional<FishingTableDefinition> tableOpt = findTable(context);
        if (tableOpt.isEmpty()) return Optional.empty();
        FishingTableDefinition table = tableOpt.get();

        if (context.hook() != null && context.hook().isTreasureOnly()) {
            return Optional.empty();
        }
        if (PeaceTreaty.applies(context.player())) {
            return Optional.empty();
        }
        if (table.weightedSeaCreatures()) {
            return tryResolveWeightedSeaCreature(context, table);
        }

        double seaCreatureChance = getTotalStatistic(context, ItemStatistic.SEA_CREATURE_CHANCE);
        for (FishingTableDefinition.SeaCreatureRoll roll : table.seaCreatures()) {
            SeaCreatureDefinition definition = FishingRegistry.getSeaCreature(roll.seaCreatureId());
            if (definition != null && context.player().getSkills().getCurrentLevel(net.swofty.type.skyblockgeneric.skill.SkillCategories.FISHING) < definition.requiredFishingLevel()) {
                continue;
            }
            if (definition != null && !definition.isAvailable(context)) {
                continue;
            }
            double tagBonus = definition == null ? 0.0D : getTagBonus(context, definition.tags());
            if (Math.random() * 100 <= roll.chance() + seaCreatureChance + tagBonus) {
                double skillXp = definition == null ? 0.0D : definition.skillXp();
                CatchPayload.SeaCreature payload = FishingFestival.replaceWithShark(context,
                    new CatchPayload.SeaCreature(roll.seaCreatureId(), skillXp));
                if (rollDoubleHook(context)) {
                    payload = payload.withDoubleHook();
                }
                return Optional.of(payload);
            }
        }
        return Optional.empty();
    }

    private static Optional<CatchPayload> tryResolveWeightedSeaCreature(FishingContext context, FishingTableDefinition table) {
        if (Math.random() * 100 >= playerSeaCreatureChance(context)) {
            return Optional.empty();
        }

        int fishingLevel = context.player().getSkills().getCurrentLevel(net.swofty.type.skyblockgeneric.skill.SkillCategories.FISHING);
        List<SeaCreatureWeights.Candidate> eligible = new ArrayList<>();
        for (FishingTableDefinition.SeaCreatureRoll roll : table.seaCreatures()) {
            SeaCreatureDefinition definition = FishingRegistry.getSeaCreature(roll.seaCreatureId());
            if (definition == null || fishingLevel < definition.requiredFishingLevel() || !definition.isAvailable(context)) {
                continue;
            }
            eligible.add(new SeaCreatureWeights.Candidate(roll.seaCreatureId(), roll.weight(), rarityOf(definition)));
        }
        if (eligible.isEmpty()) return Optional.empty();

        SeaCreatureWeights.Candidate picked = SeaCreatureWeights.pick(eligible, weightModifiers(context), ThreadLocalRandom.current());
        SeaCreatureDefinition definition = FishingRegistry.getSeaCreature(picked.id());
        CatchPayload.SeaCreature payload = new CatchPayload.SeaCreature(picked.id(), definition == null ? 0 : definition.skillXp());
        if (rollDoubleHook(context)) {
            payload = payload.withDoubleHook();
        }
        return Optional.of(payload);
    }

    private static SeaCreatureWeights.Modifiers weightModifiers(FishingContext context) {
        String hookId = context.hook() == null ? null : context.hook().getItemId();
        String baitId = context.bait() == null ? null : context.bait().getItemId();
        double tracking = context.player().getStatistics().allStatistics().getOverall(ItemStatistic.TRACKING);
        double drakePiper = EssencePerks.value(context.player(), IceEssencePerk.DRAKE_PIPER);
        return new SeaCreatureWeights.Modifiers(hookId, baitId, TimeOfDayTag.NIGHT.isAvailable(context), tracking,
            Map.of("REINDRAKE", 1 + drakePiper / 100D));
    }

    private static Rarity rarityOf(SeaCreatureDefinition definition) {
        for (FishingTag tag : definition.tags()) {
            if (tag instanceof RarityTag rarity) return Rarity.valueOf(rarity.id());
        }
        return null;
    }

    private static double playerSeaCreatureChance(FishingContext context) {
        return playerStatistic(context, ItemStatistic.SEA_CREATURE_CHANCE);
    }

    public static double playerStatistic(FishingContext context, ItemStatistic statistic) {
        double total = context.player().getStatistics().allStatistics().getOverall(statistic);
        ItemType held = new SkyBlockItem(context.player().getItemInMainHand()).getAttributeHandler().getPotentialType();
        if (held != context.rod().getAttributeHandler().getPotentialType()) {
            total += context.rod().getAttributeHandler().getStatistics().getOverall(statistic);
        }
        total += FishingRodPartService.getStatistics(context.rod()).getOverall(statistic);
        total += context.hotspotBuffs().getOverall(statistic);
        if (context.bait() != null) {
            SkyBlockItem baitItem = FishingItemSupport.getItem(context.bait().getItemId());
            if (baitItem != null) {
                total += baitItem.getAttributeHandler().getStatistics().getOverall(statistic);
            }
        }
        return total;
    }

    private static CatchPayload resolveItem(FishingContext context) {
        Optional<FishingTableDefinition> tableOpt = findTable(context);
        if (tableOpt.isEmpty()) return DEFAULT_CATCH;
        FishingTableDefinition table = tableOpt.get();

        Optional<CatchPayload> forced = forcedCatch(context, table.treasures(), true)
            .or(() -> forcedCatch(context, table.items(), false))
            .or(() -> forcedCatch(context, table.junk(), false));
        if (forced.isPresent()) return forced.get();

        List<FishingTableDefinition.LootEntry> pool = table.items();
        double treasureChance = getTotalStatistic(context, ItemStatistic.TREASURE_CHANCE);
        if (context.bait() != null) {
            treasureChance += context.bait().getTreasureChanceBonus();
        }
        if (context.sinker() != null && context.sinker().isBayouTreasureToJunk()) {
            treasureChance += 10.0D;
        }

        if (!table.treasures().isEmpty() && Math.random() * 100 <= treasureChance) {
            Optional<CatchPayload> essence = rollEssenceTreasure(table);
            if (essence.isPresent()) return essence.get();
            pool = context.sinker() != null && context.sinker().isBayouTreasureToJunk() ? table.junk() : table.treasures();
            return pick(pool, true).orElse(DEFAULT_CATCH);
        }

        return pick(pool, false)
            .or(() -> table.junk().isEmpty() ? Optional.empty() : pick(table.junk(), false))
            .orElse(DEFAULT_CATCH);
    }

    private static Optional<CatchPayload> rollEssenceTreasure(FishingTableDefinition table) {
        FishingTableDefinition.EssenceTreasure essence = table.essenceTreasure();
        if (essence == null || Math.random() * 100 >= essence.chance()) return Optional.empty();
        int amount = ThreadLocalRandom.current().nextInt(essence.min(), essence.max() + 1);
        return Optional.of(new CatchPayload.Essence(essence.type(), amount, essence.skillXp()));
    }

    private static Optional<CatchPayload> forcedCatch(FishingContext context, List<FishingTableDefinition.LootEntry> pool,
                                                      boolean fromTreasure) {
        ItemType claimed = LootRoll.claim(context.player().getUuid(),
            pool.stream().map(entry -> ItemType.get(entry.itemId())).toList());
        if (claimed == null) return Optional.empty();
        return pool.stream().filter(entry -> ItemType.get(entry.itemId()) == claimed).findFirst()
            .map(entry -> new CatchPayload.Item(entry.itemId(), entry.amount(), entry.skillXp(), fromTreasure));
    }

    private static Optional<CatchPayload> pick(List<FishingTableDefinition.LootEntry> pool, boolean fromTreasure) {
        if (pool.isEmpty()) return Optional.empty();

        double roll = Math.random() * 100;
        double cursor = 0;
        for (FishingTableDefinition.LootEntry entry : pool) {
            cursor += entry.chance();
            if (roll <= cursor) {
                return Optional.of(new CatchPayload.Item(entry.itemId(), entry.amount(), entry.skillXp(), fromTreasure));
            }
        }
        FishingTableDefinition.LootEntry first = pool.getFirst();
        return Optional.of(new CatchPayload.Item(first.itemId(), first.amount(), first.skillXp(), fromTreasure));
    }

    private static Optional<FishingTableDefinition> findTable(FishingContext context) {
        FishingTableDefinition fallback = null;
        String serverType = currentServerType();
        for (FishingTableDefinition definition : FishingRegistry.getTables()) {
            if (!definition.mediums().isEmpty() && !definition.mediums().contains(context.medium())) {
                continue;
            }
            if (context.regionId() != null && definition.regions().contains(context.regionId())) {
                return Optional.of(definition);
            }
            if (serverType != null && definition.serverTypes().contains(serverType)) {
                return Optional.of(definition);
            }
            if (definition.regions().isEmpty() && fallback == null) {
                fallback = definition;
            }
        }
        return Optional.ofNullable(fallback);
    }

    private static String currentServerType() {
        return HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType().name();
    }

    private static boolean rollDoubleHook(FishingContext context) {
        double chance = getTotalStatistic(context, ItemStatistic.DOUBLE_HOOK_CHANCE);
        if (context.bait() != null) {
            chance += context.bait().getDoubleHookChanceBonus();
        }
        return chance > 0 && Math.random() * 100 <= chance;
    }

    private static double getTotalStatistic(FishingContext context, ItemStatistic statistic) {
        double total = context.rod().getAttributeHandler().getStatistics().getOverall(statistic)
            + FishingRodPartService.getStatistics(context.rod()).getOverall(statistic);
        total += context.hotspotBuffs().getOverall(statistic);
        if (context.bait() != null) {
            SkyBlockItem baitItem = FishingItemSupport.getItem(context.bait().getItemId());
            if (baitItem != null) {
                total += baitItem.getAttributeHandler().getStatistics().getOverall(statistic);
            }
        }
        return total;
    }

    private static double getTagBonus(FishingContext context, List<FishingTag> tags) {
        double total = 0.0D;
        if (context.hook() != null) {
            total += getTagBonus(context.hook().getTagBonuses(), tags);
        }
        if (context.line() != null) {
            total += getTagBonus(context.line().getTagBonuses(), tags);
        }
        if (context.sinker() != null) {
            total += getTagBonus(context.sinker().getTagBonuses(), tags);
        }
        if (context.bait() != null) {
            total += getTagBonus(context.bait().getTagBonuses(), tags);
        }
        return total;
    }

    private static double getTagBonus(java.util.Map<String, Double> bonuses, List<FishingTag> tags) {
        double total = 0.0D;
        for (FishingTag tag : tags) {
            total += bonuses.getOrDefault(tag.id(), 0.0D);
        }
        return total;
    }
}
