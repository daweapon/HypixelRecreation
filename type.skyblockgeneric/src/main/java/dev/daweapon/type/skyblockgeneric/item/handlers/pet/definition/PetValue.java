package dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition;

import dev.daweapon.commons.skyblock.item.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A number that grows with pet level: {@code base + per_level × (level − baseLevel)}, optionally
 * clamped. baseLevel is 0 for normal pets and 100 for the level-200 dragons.
 */
public record PetValue(RarityNumbers base, RarityNumbers perLevel, @Nullable Double min, @Nullable Double max) {

    public double at(Rarity rarity, int level, int baseLevel) {
        double value = base.get(rarity) + perLevel.get(rarity) * Math.max(0, level - baseLevel);
        if (min != null) value = Math.max(min, value);
        if (max != null) value = Math.min(max, value);
        return value;
    }

    public static PetValue parse(Object raw, String where) {
        if (raw instanceof Number number) {
            return new PetValue(RarityNumbers.constant(number.doubleValue()), RarityNumbers.ZERO, null, null);
        }
        if (!(raw instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException(where + ": a value must be a number or a map with base/per_level");
        }
        if (!map.containsKey("base") && !map.containsKey("per_level")) {
            throw new IllegalArgumentException(where + ": a value map needs base and/or per_level");
        }
        return new PetValue(
                RarityNumbers.parse(map.get("base"), where + ".base"),
                RarityNumbers.parse(map.get("per_level"), where + ".per_level"),
                map.get("min") instanceof Number n ? n.doubleValue() : null,
                map.get("max") instanceof Number n ? n.doubleValue() : null);
    }
}
