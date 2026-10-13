package net.swofty.commons.skyblock.item.attribute.attributes;

import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public class ItemAttributeDrillData extends ItemAttribute<ItemAttributeDrillData.DrillData> {
    private static final String EMPTY_SLOT = "-";
    private static final String SEPARATOR = "\\|";

    @Override
    public String getKey() {
        return "drill_data";
    }

    @Override
    public DrillData getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
        return DrillData.EMPTY;
    }

    @Override
    public DrillData loadFromString(String string) {
        String[] parts = string.split(SEPARATOR, -1);
        if (parts.length != 4) return DrillData.EMPTY;
        try {
            return new DrillData(Integer.parseInt(parts[0]), slot(parts[1]), slot(parts[2]), slot(parts[3]));
        } catch (NumberFormatException exception) {
            return DrillData.EMPTY;
        }
    }

    @Override
    public String saveIntoString() {
        if (value.equals(DrillData.EMPTY)) return null;
        return value.fuel() + "|" + save(value.tank()) + "|" + save(value.engine()) + "|" + save(value.module());
    }

    private static @Nullable String slot(String raw) {
        return raw.equals(EMPTY_SLOT) ? null : raw;
    }

    private static String save(@Nullable String slot) {
        return slot == null ? EMPTY_SLOT : slot;
    }

    public record DrillData(int fuel, @Nullable String tank, @Nullable String engine, @Nullable String module) {
        public static final int BASE_FUEL = 3_000;
        public static final DrillData EMPTY = new DrillData(BASE_FUEL, null, null, null);

        public DrillData withFuel(int fuel) {
            return new DrillData(fuel, tank, engine, module);
        }

        public DrillData withTank(@Nullable String tank) {
            return new DrillData(fuel, tank, engine, module);
        }

        public DrillData withEngine(@Nullable String engine) {
            return new DrillData(fuel, this.tank, engine, module);
        }

        public DrillData withModule(@Nullable String module) {
            return new DrillData(fuel, tank, engine, module);
        }
    }
}
