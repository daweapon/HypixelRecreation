package net.swofty.type.skyblockgeneric.fishing.trophy;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.fishing.FishingContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A requirement a Trophy Fish's cast must meet, as configured under {@code conditions:} in
 * {@code fishing/trophy_fish.yml}.
 */
public sealed interface TrophyCondition {

    boolean test(FishingContext context);

    record InRegions(Set<String> regions) implements TrophyCondition {
        @Override
        public boolean test(FishingContext context) {
            return context.regionId() != null && regions.contains(context.regionId());
        }
    }

    record MinBobberSeconds(double seconds) implements TrophyCondition {
        @Override
        public boolean test(FishingContext context) {
            return context.castDurationMs() >= seconds * 1000D;
        }
    }

    record UsingBait(Set<String> baitIds) implements TrophyCondition {
        @Override
        public boolean test(FishingContext context) {
            return context.bait() != null && baitIds.contains(context.bait().getItemId());
        }
    }

    record StarterRodWithoutEnchantments() implements TrophyCondition {
        @Override
        public boolean test(FishingContext context) {
            return context.rod().getAttributeHandler().getPotentialType() == ItemType.STARTER_LAVA_ROD
                    && context.rod().getAttributeHandler().getEnchantments().findAny().isEmpty();
        }
    }

    record MinMaxMana(double mana) implements TrophyCondition {
        @Override
        public boolean test(FishingContext context) {
            return context.player().getMaxMana() >= mana;
        }
    }

    record BobberBelowPlayer(double blocks) implements TrophyCondition {
        @Override
        public boolean test(FishingContext context) {
            return context.player().getPosition().y() - context.hookPosition().y() >= blocks;
        }
    }

    static TrophyCondition parse(Map<String, Object> raw) {
        String type = String.valueOf(raw.get("type")).toUpperCase();
        return switch (type) {
            case "REGIONS" -> new InRegions(Set.copyOf(strings(raw.get("regions"))));
            case "MIN_BOBBER_SECONDS" -> new MinBobberSeconds(number(raw, "seconds"));
            case "BAIT" -> new UsingBait(Set.copyOf(strings(raw.get("baits"))));
            case "STARTER_ROD_NO_ENCHANTMENTS" -> new StarterRodWithoutEnchantments();
            case "MIN_MANA" -> new MinMaxMana(number(raw, "mana"));
            case "BOBBER_BELOW_PLAYER" -> new BobberBelowPlayer(number(raw, "blocks"));
            default -> throw new IllegalArgumentException("Unknown trophy fish condition " + type);
        };
    }

    private static List<String> strings(Object value) {
        return value instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
    }

    private static double number(Map<String, Object> raw, String key) {
        return raw.get(key) instanceof Number number ? number.doubleValue() : 0D;
    }
}
