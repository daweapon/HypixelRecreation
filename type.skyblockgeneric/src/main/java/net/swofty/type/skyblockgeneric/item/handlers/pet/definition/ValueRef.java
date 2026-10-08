package net.swofty.type.skyblockgeneric.item.handlers.pet.definition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A number in an ability's effect block: either a literal, or {@code "{i}"} for the ability's
 * i-th value at the pet's current level.
 */
public record ValueRef(double literal, int index) {
    private static final Pattern REFERENCE = Pattern.compile("\\{(\\d+)}");

    public double resolve(double[] values) {
        if (index < 0) return literal;
        return index < values.length ? values[index] : 0;
    }

    public static ValueRef parse(Object raw, int valueCount, String where) {
        if (raw instanceof Number number) return new ValueRef(number.doubleValue(), -1);
        Matcher matcher = REFERENCE.matcher(String.valueOf(raw).trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(where + ": expected a number or \"{i}\", got " + raw);
        }
        int index = Integer.parseInt(matcher.group(1));
        if (index >= valueCount) {
            throw new IllegalArgumentException(where + ": {" + index + "} but the ability has " + valueCount + " values");
        }
        return new ValueRef(0, index);
    }
}
