package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.swofty.type.skyblockgeneric.garden.GardenCrop;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class GreenhouseRules {
    public static final double MIN_WATER = -100;
    public static final double MAX_WATER = 100;
    public static final double MIN_STAGE_FACTOR = 0.2;

    private GreenhouseRules() {
    }

    public static int uniqueCrops(GreenhouseData data, GreenhouseConfig config) {
        java.util.Set<String> types = new java.util.HashSet<>();
        for (GreenhouseData.Bed bed : data.getBeds().values()) {
            for (GreenhouseData.Plant plant : bed.getPlants().values()) {
                PlantType type = config.plant(plant.getType());
                if (type != null && type.baseCrop()) types.add(type.id());
            }
        }
        return Math.min(types.size(), config.maxUniqueCrops());
    }

    public static double uniquePercent(GreenhouseData data, GreenhouseConfig config) {
        return uniqueCrops(data, config) * config.uniqueCropPercent();
    }

    public static long stageMillis(GreenhouseConfig config, int speedTier, double uniquePercent) {
        double factor = Math.max(MIN_STAGE_FACTOR, 1 - (config.growthSpeedPercent(speedTier) + uniquePercent) / 100.0);
        return Math.round(config.stageHours() * 3_600_000L * factor);
    }

    public static Map<Integer, List<GreenhouseEffect>> appliedEffects(GreenhouseData.Bed bed, GreenhouseConfig config) {
        Map<Integer, List<GreenhouseEffect>> direct = new java.util.TreeMap<>();
        for (int slot : bed.getPlants().keySet()) {
            List<GreenhouseEffect> list = new ArrayList<>();
            for (int[] cell : GreenhouseLayout.neighbours(slot)) {
                GreenhouseData.Plant neighbour = bed.getPlants().get(GreenhouseData.slot(cell[0], cell[1]));
                PlantType type = neighbour == null ? null : config.plant(neighbour.getType());
                if (type == null) continue;
                for (GreenhouseEffect effect : type.effects()) {
                    if (effect != GreenhouseEffect.EFFECT_SPREAD) list.add(effect);
                }
            }
            direct.put(slot, list);
        }
        Map<Integer, List<GreenhouseEffect>> result = new java.util.TreeMap<>();
        for (int slot : bed.getPlants().keySet()) {
            List<GreenhouseEffect> list = new ArrayList<>(direct.get(slot));
            for (int[] cell : GreenhouseLayout.neighbours(slot)) {
                int other = GreenhouseData.slot(cell[0], cell[1]);
                GreenhouseData.Plant neighbour = bed.getPlants().get(other);
                PlantType type = neighbour == null ? null : config.plant(neighbour.getType());
                if (type != null && type.effects().contains(GreenhouseEffect.EFFECT_SPREAD)) {
                    direct.get(other).stream().filter(GreenhouseEffect::spreadable).forEach(list::add);
                }
            }
            if (list.contains(GreenhouseEffect.IMMUNITY)) list.removeIf(GreenhouseEffect::isNegative);
            result.put(slot, list);
        }
        return result;
    }

    public static double sum(List<GreenhouseEffect> effects, GreenhouseEffect... kinds) {
        double total = 0;
        for (GreenhouseEffect effect : effects) {
            for (GreenhouseEffect kind : kinds) if (effect == kind) total += effect.getMagnitude();
        }
        return total;
    }

    public static double yieldMultiplier(List<GreenhouseEffect> effects, double plantYieldPercent, double uniquePercent) {
        return 1 + sum(effects, GreenhouseEffect.HARVEST_BOOST, GreenhouseEffect.IMPROVED_HARVEST_BOOST)
                - sum(effects, GreenhouseEffect.HARVEST_LOSS) + (plantYieldPercent + uniquePercent) / 100.0;
    }

    public static double xpMultiplier(List<GreenhouseEffect> effects) {
        return Math.max(0, 1 + sum(effects, GreenhouseEffect.XP_BOOST, GreenhouseEffect.IMPROVED_XP_BOOST)
                - sum(effects, GreenhouseEffect.XP_LOSS));
    }

    public static double waterLossFactor(List<GreenhouseEffect> effects) {
        return Math.max(0, 1 - sum(effects, GreenhouseEffect.WATER_RETAIN, GreenhouseEffect.IMPROVED_WATER_RETAIN)
                + sum(effects, GreenhouseEffect.WATER_DRAIN));
    }

    public static double greenhouseSowdustMultiplier(GardenCrop crop) {
        return switch (crop) {
            case WHEAT -> 1;
            case CARROT -> 3.5;
            case POTATO, NETHER_WART -> 3;
            case PUMPKIN -> 0.85;
            case SUGAR_CANE, COCOA_BEANS, MOONFLOWER, SUNFLOWER, WILD_ROSE -> 2;
            case MELON -> 4;
            case CACTUS -> 1.5;
            case MUSHROOM -> 0.95;
        };
    }

    public static double sowdust(GardenCrop crop, double baseAmount, double fortune) {
        return Math.round(baseAmount / (5 * greenhouseSowdustMultiplier(crop))) * 5 * (1 + Math.max(0, fortune) / 100.0);
    }

    public static int harvestAmount(PlantType type, double fortune, double multiplier) {
        return (int) Math.max(0, Math.round(type.baseYield() * (1 + Math.max(0, fortune) / 100.0) * Math.max(0, multiplier)));
    }

    public static double waterLoss(GreenhouseConfig config, List<GreenhouseEffect> effects, RandomGenerator random) {
        int min = config.waterLossMin();
        int max = config.waterLossMax();
        double base = min + random.nextDouble() * (max - min);
        return base * waterLossFactor(effects);
    }

    public static boolean advances(GreenhouseConfig config, double water, RandomGenerator random) {
        return water >= 0 || random.nextDouble() < config.unwateredAdvanceChance();
    }

    public static boolean decayed(GreenhouseConfig config, GreenhouseData.Plant plant, boolean waterable, long now) {
        long day = 86_400_000L;
        return waterable
                ? now - plant.getLastWateredAt() > config.decayDaysSinceWatering() * day
                : now - plant.getPlantedAt() > config.decayDaysUnwaterable() * day;
    }

    public static Map<GreenhouseEffect, Integer> counts(List<GreenhouseEffect> effects) {
        Map<GreenhouseEffect, Integer> counts = new EnumMap<>(GreenhouseEffect.class);
        effects.forEach(effect -> counts.merge(effect, 1, Integer::sum));
        return counts;
    }

    public static RandomGenerator random() {
        return ThreadLocalRandom.current();
    }
}
