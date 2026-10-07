package net.swofty.type.skyblockgeneric.utility;

import java.util.Locale;
import java.util.OptionalLong;

public final class CoinAmountParser {
    private CoinAmountParser() {}

    public static OptionalLong parse(String input) {
        if (input == null) return OptionalLong.empty();
        String cleaned = input.replace(",", "").replace("_", "").trim().toLowerCase(Locale.ROOT);
        if (cleaned.isEmpty()) return OptionalLong.empty();

        double multiplier = switch (cleaned.charAt(cleaned.length() - 1)) {
            case 'k' -> 1_000D;
            case 'm' -> 1_000_000D;
            case 'b' -> 1_000_000_000D;
            default -> 1D;
        };
        if (multiplier != 1D) cleaned = cleaned.substring(0, cleaned.length() - 1);

        try {
            double value = Double.parseDouble(cleaned) * multiplier;
            if (!Double.isFinite(value) || value < 1 || value > Long.MAX_VALUE) return OptionalLong.empty();
            return OptionalLong.of((long) Math.floor(value));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }
}
