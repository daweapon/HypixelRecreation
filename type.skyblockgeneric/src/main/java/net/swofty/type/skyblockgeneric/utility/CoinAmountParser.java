package net.swofty.type.skyblockgeneric.utility;

import java.util.Locale;
import java.util.OptionalLong;

// Reads a typed coin amount: plain numbers, commas, and k / m / b / t shorthand such as 5k, 1.5m or 60b.
public final class CoinAmountParser {
    private CoinAmountParser() {}

    public static OptionalLong parse(String input) {
        try {
            double value = parseDouble(input);
            if (value < 1 || value > Long.MAX_VALUE) return OptionalLong.empty();
            return OptionalLong.of((long) Math.floor(value));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }

    public static long parseLong(String input) throws NumberFormatException {
        double value = parseDouble(input);
        if (value > Long.MAX_VALUE) throw new NumberFormatException("Too large: " + input);
        return (long) Math.floor(value);
    }

    public static double parseDouble(String input) throws NumberFormatException {
        if (input == null) throw new NumberFormatException("null");
        String cleaned = input.replace(",", "").replace("_", "").trim().toLowerCase(Locale.ROOT);
        if (cleaned.isEmpty()) throw new NumberFormatException("empty");

        double multiplier = switch (cleaned.charAt(cleaned.length() - 1)) {
            case 'k' -> 1_000D;
            case 'm' -> 1_000_000D;
            case 'b' -> 1_000_000_000D;
            case 't' -> 1_000_000_000_000D;
            default -> 1D;
        };
        if (multiplier != 1D) cleaned = cleaned.substring(0, cleaned.length() - 1);

        double value = Double.parseDouble(cleaned) * multiplier;
        if (!Double.isFinite(value)) throw new NumberFormatException("Not finite: " + input);
        return value;
    }
}
