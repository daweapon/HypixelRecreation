package net.swofty.type.skyblockgeneric.garden.visitor;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public final class VisitorGenerator {
    public static final double FARMING_XP_PER_ITEM = 4;
    public static final double COPPER_FACTOR = 1.3;
    public static final int MINIMUM_COPPER = 2;
    private static final List<String> CROP_FAMILIES = List.of(
            "WHEAT", "CARROT", "POTATO", "PUMPKIN", "SUGAR_CANE", "MELON", "CACTUS", "COCOA_BEANS", "MUSHROOM", "NETHER_WART");
    private static final int REROLLS = 25;
    private static final int DEFAULT_FIXED_AMOUNT = 3;

    private VisitorGenerator() {
    }

    public static @Nullable VisitorCatalogue.Visitor pick(VisitorCatalogue catalogue, Predicate<VisitorCatalogue.Visitor> eligible,
                                                         Set<String> present, double rareBonus, RandomGenerator random) {
        List<VisitorCatalogue.Visitor> candidates = catalogue.visitors().stream()
                .filter(visitor -> !present.contains(visitor.id()) && eligible.test(visitor))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        for (int attempt = 0; attempt < REROLLS; attempt++) {
            VisitorRarity rarity = rollRarity(catalogue, rareBonus, random);
            List<VisitorCatalogue.Visitor> pool = candidates.stream().filter(visitor -> visitor.rarity() == rarity).toList();
            if (!pool.isEmpty()) {
                return pool.get(random.nextInt(pool.size()));
            }
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    static VisitorRarity rollRarity(VisitorCatalogue catalogue, double rareBonus, RandomGenerator random) {
        double total = 0;
        for (VisitorRarity rarity : VisitorRarity.values()) {
            total += weight(catalogue, rarity, rareBonus);
        }
        double roll = random.nextDouble() * total;
        for (VisitorRarity rarity : VisitorRarity.values()) {
            roll -= weight(catalogue, rarity, rareBonus);
            if (roll < 0) {
                return rarity;
            }
        }
        return VisitorRarity.UNCOMMON;
    }

    private static double weight(VisitorCatalogue catalogue, VisitorRarity rarity, double rareBonus) {
        double weight = catalogue.weight(rarity);
        return rarity == VisitorRarity.UNCOMMON ? weight : weight * (1 + rareBonus);
    }

    public static VisitorOffer generate(VisitorCatalogue catalogue, VisitorCatalogue.Visitor visitor, int gardenLevel, RandomGenerator random) {
        List<VisitorOffer.Required> required = new ArrayList<>();
        double baseItems = 0;
        double baseQuantityTotal = 0;
        double copperMultiplier = catalogue.copperMultiplier(visitor.rarity());
        VisitorCatalogue.Desire desire = visitor.desire();

        if (desire.mode().equals("FIXED_ITEMS") && !desire.items().isEmpty()) {
            for (VisitorCatalogue.DesireItem item : desire.items()) {
                if (item.item() == null) continue;
                int amount = item.amount() == null ? DEFAULT_FIXED_AMOUNT : item.amount();
                required.add(new VisitorOffer.Required(item.item(), item.name(), amount, amount));
                baseItems += amount;
                baseQuantityTotal += amount;
            }
        } else {
            for (String familyId : chooseFamilies(desire, random)) {
                VisitorCatalogue.Family family = catalogue.family(familyId);
                if (family == null) continue;
                int[] range = catalogue.baseQuantity(gardenLevel);
                double base = range[0] + random.nextDouble() * (range[1] - range[0]);
                double raw = base * family.multiplier() * catalogue.quantityMultiplier(visitor.rarity());
                required.add(compact(family, raw));
                baseItems += raw;
                baseQuantityTotal += raw / catalogue.quantityMultiplier(visitor.rarity());
            }
        }
        if (required.isEmpty()) {
            VisitorCatalogue.Family fallback = catalogue.family("WHEAT");
            int[] range = catalogue.baseQuantity(gardenLevel);
            double raw = (range[0] + random.nextDouble() * (range[1] - range[0])) * fallback.multiplier()
                    * catalogue.quantityMultiplier(visitor.rarity());
            required.add(compact(fallback, raw));
            baseItems = raw;
            baseQuantityTotal = raw / catalogue.quantityMultiplier(visitor.rarity());
        }

        long farmingXp = Math.round(baseItems * FARMING_XP_PER_ITEM * catalogue.farmingXpMultiplier(visitor.rarity()));
        int copper = (int) Math.max(MINIMUM_COPPER, Math.round(baseQuantityTotal / 2000 * copperMultiplier * COPPER_FACTOR));
        return new VisitorOffer(required, farmingXp, catalogue.gardenXp(gardenLevel, visitor.rarity()), copper, catalogue.bits());
    }

    static VisitorOffer.Required compact(VisitorCatalogue.Family family, double raw) {
        VisitorCatalogue.FamilyItem chosen = family.items().getFirst();
        for (VisitorCatalogue.FamilyItem item : family.items()) {
            if (item.rawUnits() <= raw && item.rawUnits() >= chosen.rawUnits()) {
                chosen = item;
            }
        }
        int amount = (int) Math.floor(raw / chosen.rawUnits());
        double leftover = raw - amount * chosen.rawUnits();
        if (leftover < raw * 0.1) {
            amount++;
        }
        amount = Math.max(1, amount);
        return new VisitorOffer.Required(chosen.item(), chosen.name(), amount, amount);
    }

    static List<String> chooseFamilies(VisitorCatalogue.Desire desire, RandomGenerator random) {
        List<String> pool = desire.families().isEmpty() ? new ArrayList<>(CROP_FAMILIES) : new ArrayList<>(desire.families());
        return switch (desire.mode()) {
            case "ONE_FAMILY" -> List.of(pool.getFirst());
            case "ALL_FAMILIES" -> pool;
            case "N_FAMILIES" -> {
                Collections.shuffle(pool, new java.util.Random(random.nextLong()));
                yield pool.subList(0, Math.min(Math.max(1, desire.count()), pool.size()));
            }
            default -> List.of(pool.get(random.nextInt(pool.size())));
        };
    }
}
