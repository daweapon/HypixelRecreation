package dev.daweapon.type.skyblockgeneric.drill;

import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeDrillData.DrillData;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DrillParts {
    private static final Map<String, DrillPart> PARTS = new LinkedHashMap<>();

    static {
        register(DrillPart.tank("MITHRIL_FUEL_TANK", 10_000, 2));
        register(DrillPart.tank("TITANIUM_FUEL_TANK", 25_000, 4));
        register(DrillPart.tank("GEMSTONE_FUEL_TANK", 50_000, 6));
        register(DrillPart.tank("PERFECTLY_CUT_FUEL_TANK", 100_000, 10));

        register(DrillPart.engine("MITHRIL_DRILL_ENGINE", 75, 5));
        register(DrillPart.engine("TITANIUM_DRILL_ENGINE", 150, 15));
        register(DrillPart.engine("RUBY_POLISHED_DRILL_ENGINE", 250, 30));
        register(DrillPart.engine("SAPPHIRE_POLISHED_DRILL_ENGINE", 400, 50));
        register(DrillPart.engine("AMBER_POLISHED_DRILL_ENGINE", 600, 100));

        register(DrillPart.module("GOBLIN_OMELETTE").withFuelSave(25));
        register(DrillPart.module("GOBLIN_OMELETTE_PESTO").withFuelSave(75));
        register(DrillPart.module("GOBLIN_OMELETTE_SPICY").withPowder(20));
        register(DrillPart.module("GOBLIN_OMELETTE_SUNNY_SIDE").withStats(0, 50).withFuelMultiplier(4));
        register(DrillPart.module("GOBLIN_OMELETTE_BLUE_CHEESE").withPerkLevels(1));
        register(DrillPart.module("STARFALL_SEASONING").withStats(25, 10));
        register(DrillPart.module("TUNGSTEN_KEYCHAIN").withRegulation(500));
    }

    private DrillParts() {
    }

    private static void register(DrillPart part) {
        PARTS.put(part.itemId(), part);
    }

    public static @Nullable DrillPart get(@Nullable String itemId) {
        return itemId == null ? null : PARTS.get(itemId);
    }

    public static List<DrillPart> all() {
        return List.copyOf(PARTS.values());
    }

    public static @Nullable String installed(DrillData data, DrillSlot slot) {
        return switch (slot) {
            case TANK -> data.tank();
            case ENGINE -> data.engine();
            case MODULE -> data.module();
        };
    }

    public static DrillData install(DrillData data, DrillPart part) {
        return switch (part.slot()) {
            case TANK -> data.withTank(part.itemId());
            case ENGINE -> data.withEngine(part.itemId());
            case MODULE -> data.withModule(part.itemId());
        };
    }

    public static DrillData remove(DrillData data, DrillSlot slot) {
        DrillData removed = switch (slot) {
            case TANK -> data.withTank(null);
            case ENGINE -> data.withEngine(null);
            case MODULE -> data.withModule(null);
        };
        return removed.withFuel(Math.min(removed.fuel(), DrillFuel.capacity(removed)));
    }

    public static List<DrillPart> installedParts(DrillData data) {
        return java.util.stream.Stream.of(data.tank(), data.engine(), data.module())
                .map(DrillParts::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public static ItemStatistics statistics(DrillData data) {
        double speed = 0;
        double fortune = 0;
        for (DrillPart part : installedParts(data)) {
            speed += part.miningSpeed();
            fortune += part.miningFortune();
        }
        if (speed == 0 && fortune == 0) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        if (speed != 0) builder.withBase(ItemStatistic.MINING_SPEED, speed);
        if (fortune != 0) builder.withBase(ItemStatistic.MINING_FORTUNE, fortune);
        return builder.build();
    }

    public static double cooldownReductionPercent(DrillData data) {
        DrillPart tank = get(data.tank());
        return tank == null ? 0 : tank.cooldownReduction();
    }

    public static double fuelSaveChance(DrillData data) {
        return installedParts(data).stream().mapToDouble(DrillPart::fuelSaveChance).max().orElse(0);
    }

    public static int fuelMultiplier(DrillData data) {
        return installedParts(data).stream().mapToInt(DrillPart::fuelMultiplier).max().orElse(1);
    }

    public static double powderPercent(DrillData data) {
        return installedParts(data).stream().mapToDouble(DrillPart::powderPercent).sum();
    }

    public static int perkLevels(DrillData data) {
        return installedParts(data).stream().mapToInt(DrillPart::perkLevels).sum();
    }

    public static int fuelPerRegulation(DrillData data) {
        return installedParts(data).stream().mapToInt(DrillPart::fuelPerRegulation).max().orElse(0);
    }

    public static List<String> effectLines(DrillPart part) {
        List<String> lines = new java.util.ArrayList<>();
        if (part.maxFuel() > 0) {
            lines.add("<2>" + DrillFuel.format(part.maxFuel()) + " Max Fuel Capacity");
            lines.add("<a>-" + trim(part.cooldownReduction()) + "% Pickaxe Ability Cooldown");
        }
        if (part.miningSpeed() != 0) lines.add("<6>+" + trim(part.miningSpeed()) + " Mining Speed");
        if (part.miningFortune() != 0) lines.add("<6>+" + trim(part.miningFortune()) + " Mining Fortune");
        if (part.fuelSaveChance() > 0) {
            lines.add("<7>Grants a <a>" + trim(part.fuelSaveChance()) + "% <7>chance to not consume <2>Fuel <7>when mining.");
        }
        if (part.fuelMultiplier() > 1) lines.add("<7>This Drill consumes <c>quadruple <7>the <2>Fuel<7>.");
        if (part.powderPercent() > 0) lines.add("<7>Earn <9>+" + trim(part.powderPercent()) + "% Powder <7>from all sources.");
        if (part.perkLevels() > 0) lines.add("<7>Adds <a>+" + part.perkLevels() + " Level <7>to all of your unlocked <5>Heart of the Mountain <7>perks.");
        if (part.fuelPerRegulation() > 0) {
            lines.add("<7>Decreases your <c>Heat <7>or <b>Cold <7>by <a>1 <7>for every <2>" + part.fuelPerRegulation() + " Fuel <7>used.");
        }
        return lines;
    }

    private static String trim(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
