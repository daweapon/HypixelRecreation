package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHunting;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Determines the shard cost and possible outcomes for fusing two hunting attribute shards
// together, and performs the fusion against a player's hunting data.
public final class AttributeFusionService {
    private static final int MAX_RESULTS = 3;
    private static final int SPECIAL_AMOUNT = 2;
    private static final double PURE_REPTILE_PERCENT_PER_LEVEL = 2;

    private AttributeFusionService() {
    }

    public static int required(AttributeDefinition definition) {
        if (definition.fusionCost() > 0) return definition.fusionCost();
        String family = definition.family().name().toLowerCase(Locale.ROOT);
        if (isChameleon(definition)) return 1;
        if (family.contains("reptile") || family.contains("amphibian") || family.contains("elemental")) return 2;
        return 5;
    }

    public static int requiredOf(AttributeDefinition shard, AttributeDefinition first, AttributeDefinition second) {
        return first.id().equals(second.id()) ? required(shard) * 2 : required(shard);
    }

    public static List<FusionResult> results(AttributeDefinition first, AttributeDefinition second) {
        if (first == null || second == null) return List.of();
        if (isChameleon(first) || isChameleon(second)) {
            if (isChameleon(first) && isChameleon(second)) return List.of();
            AttributeDefinition origin = isChameleon(first) ? second : first;
            return FusionData.chameleon(origin.id()).stream()
                    .filter(id -> !isInput(id, first, second))
                    .map(AttributeRegistry::get)
                    .map(value -> new FusionResult(value, 1, FusionType.CHAMELEON)).toList();
        }
        List<FusionResult> result = new ArrayList<>();
        for (SpecialFusionRecipe recipe : FusionData.recipes()) {
            if (isInput(recipe.output(), first, second) || !recipe.matches(first, second)) continue;
            result.add(new FusionResult(AttributeRegistry.get(recipe.output()), SPECIAL_AMOUNT, FusionType.SPECIAL));
        }
        for (AttributeId id : idResults(first, second)) {
            if (isInput(id, first, second) || result.stream().anyMatch(existing -> existing.definition().id().equals(id))) continue;
            result.add(new FusionResult(AttributeRegistry.get(id), 1, FusionType.ID));
        }
        return result.stream().limit(MAX_RESULTS).toList();
    }

    public static boolean fuse(SkyBlockPlayer player, AttributeDefinition first, AttributeDefinition second,
                               FusionResult result) {
        DatapointHunting.HuntingData data = player.getHuntingData();
        boolean same = first.id().equals(second.id());
        if (same ? data.shardCount(first.id()) < requiredOf(first, first, second)
                : data.shardCount(first.id()) < required(first) || data.shardCount(second.id()) < required(second))
            return false;
        data.removeShards(first.id(), requiredOf(first, first, second));
        if (!same) data.removeShards(second.id(), requiredOf(second, first, second));
        int amount = result.amount();
        int pureReptile = data.level("R45");
        if (pureReptile > 0 && (isReptile(first) || isReptile(second))
                && LootRoll.roll(player.getUuid(), AttributeShardComponent.itemType(result.definition()),
                pureReptile * PURE_REPTILE_PERCENT_PER_LEVEL)) amount *= 2;
        data.addShards(result.definition().id(), amount);
        player.getSkills().increase(player, SkillCategories.HUNTING, HuntingXp.fusion(result.definition()));
        return true;
    }


    private static boolean isInput(AttributeId id, AttributeDefinition first, AttributeDefinition second) {
        return id.equals(first.id()) || id.equals(second.id());
    }

    private static boolean isChameleon(AttributeDefinition definition) {
        return definition.shard().equalsIgnoreCase("Chameleon");
    }

    private static boolean isReptile(AttributeDefinition definition) {
        String family = definition.family().name().toLowerCase(Locale.ROOT);
        return isChameleon(definition) || "Reptile".equals(FusionData.family(definition.id()))
                || family.contains("reptile") || family.contains("lizard") || family.contains("serpent");
    }

    private static List<AttributeId> idResults(AttributeDefinition first, AttributeDefinition second) {
        AttributeId firstResult = FusionData.idResult(first.id());
        AttributeId secondResult = FusionData.idResult(second.id());
        List<AttributeId> results = new ArrayList<>();
        if (first.category() != second.category()) {
            results.add(firstResult);
            results.add(secondResult);
        } else if (first.rarity() != second.rarity()) {
            results.add(first.rarity().ordinal() > second.rarity().ordinal() ? firstResult : secondResult);
        } else {
            results.add(first.id().equals(secondResult) ? firstResult : secondResult);
        }
        return results.stream().filter(java.util.Objects::nonNull).distinct().toList();
    }

    public record FusionResult(AttributeDefinition definition, int amount, FusionType type) {
    }

    public enum FusionType {ID, SPECIAL, CHAMELEON}
}
