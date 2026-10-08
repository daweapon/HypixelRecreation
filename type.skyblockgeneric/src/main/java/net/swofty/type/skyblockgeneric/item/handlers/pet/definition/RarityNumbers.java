package net.swofty.type.skyblockgeneric.item.handlers.pet.definition;

import net.swofty.commons.skyblock.item.Rarity;

import java.util.EnumMap;
import java.util.Map;

/**
 * One number per rarity. A rarity without its own number uses the nearest lower rarity that has
 * one, then the nearest higher, so a pet never silently loses a value at a rarity.
 */
public final class RarityNumbers {
    public static final RarityNumbers ZERO = constant(0);

    private final EnumMap<Rarity, Double> values;

    private RarityNumbers(EnumMap<Rarity, Double> values) {
        this.values = values;
    }

    public static RarityNumbers constant(double value) {
        EnumMap<Rarity, Double> values = new EnumMap<>(Rarity.class);
        values.put(Rarity.COMMON, value);
        return new RarityNumbers(values);
    }

    public static RarityNumbers of(Map<Rarity, Double> values) {
        if (values.isEmpty()) return ZERO;
        return new RarityNumbers(new EnumMap<>(values));
    }

    public static RarityNumbers parse(Object raw, String where) {
        if (raw == null) return ZERO;
        if (raw instanceof Number number) return constant(number.doubleValue());
        if (raw instanceof Map<?, ?> map) {
            EnumMap<Rarity, Double> values = new EnumMap<>(Rarity.class);
            map.forEach((key, value) -> {
                Rarity rarity = Rarity.getRarity(String.valueOf(key));
                if (rarity == null) throw new IllegalArgumentException(where + ": unknown rarity '" + key + "'");
                if (!(value instanceof Number number)) {
                    throw new IllegalArgumentException(where + ": '" + key + "' must be a number");
                }
                values.put(rarity, number.doubleValue());
            });
            return of(values);
        }
        throw new IllegalArgumentException(where + ": expected a number or a rarity map, got " + raw);
    }

    public double get(Rarity rarity) {
        Double exact = values.get(rarity);
        if (exact != null) return exact;
        for (int i = rarity.ordinal() - 1; i >= 0; i--) {
            Double lower = values.get(Rarity.values()[i]);
            if (lower != null) return lower;
        }
        for (int i = rarity.ordinal() + 1; i < Rarity.values().length; i++) {
            Double higher = values.get(Rarity.values()[i]);
            if (higher != null) return higher;
        }
        return 0;
    }
}
