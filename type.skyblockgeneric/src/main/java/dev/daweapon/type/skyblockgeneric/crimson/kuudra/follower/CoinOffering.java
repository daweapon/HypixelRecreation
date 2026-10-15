package dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower;

import java.math.BigDecimal;
import java.util.Locale;

/** What a player typed into the Kuudra Believer's offering sign: digits, commas and an optional k/m/b. */
public record CoinOffering(Result result, long amount) {

    public enum Result {
        VALID,
        UNREADABLE,
        NEGATIVE
    }

    public static CoinOffering parse(String input) {
        String cleaned = input.replace(",", "").trim().toLowerCase(Locale.ROOT);
        if (cleaned.isEmpty()) return new CoinOffering(Result.UNREADABLE, 0);
        long multiplier = switch (cleaned.charAt(cleaned.length() - 1)) {
            case 'k' -> 1_000L;
            case 'm' -> 1_000_000L;
            case 'b' -> 1_000_000_000L;
            default -> 1L;
        };
        if (multiplier != 1) cleaned = cleaned.substring(0, cleaned.length() - 1);
        try {
            BigDecimal value = new BigDecimal(cleaned).multiply(BigDecimal.valueOf(multiplier));
            if (value.signum() < 0) return new CoinOffering(Result.NEGATIVE, 0);
            return new CoinOffering(Result.VALID, value.longValueExact());
        } catch (NumberFormatException | ArithmeticException exception) {
            return new CoinOffering(Result.UNREADABLE, 0);
        }
    }
}
