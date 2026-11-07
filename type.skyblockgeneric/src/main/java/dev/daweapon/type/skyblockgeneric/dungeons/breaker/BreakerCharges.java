package dev.daweapon.type.skyblockgeneric.dungeons.breaker;

import org.jetbrains.annotations.Nullable;

/**
 * A Dungeonbreaker's charges: {@code charges} were on the item at {@code since}, and two more come back every second
 * up to twenty, so the count is worked out on read instead of being ticked.
 */
public record BreakerCharges(int charges, long since) {
    public static final int MAX = 20;
    public static final int REGEN_PER_SECOND = 2;
    public static final long MILLIS_PER_CHARGE = 1_000L / REGEN_PER_SECOND;

    public BreakerCharges {
        charges = Math.max(0, Math.min(MAX, charges));
    }

    public static BreakerCharges full(long now) {
        return new BreakerCharges(MAX, now);
    }

    public static BreakerCharges decode(@Nullable String stored, long now) {
        if (stored == null || stored.isBlank()) return full(now);
        int split = stored.indexOf('@');
        try {
            if (split < 0) return new BreakerCharges(Integer.parseInt(stored.trim()), now);
            return new BreakerCharges(Integer.parseInt(stored.substring(0, split)), Long.parseLong(stored.substring(split + 1)));
        } catch (NumberFormatException exception) {
            return full(now);
        }
    }

    public String encode() {
        return charges + "@" + since;
    }

    public BreakerCharges at(long now) {
        if (charges >= MAX || now <= since) return this;
        long regained = (now - since) / MILLIS_PER_CHARGE;
        if (charges + regained >= MAX) return new BreakerCharges(MAX, now);
        return new BreakerCharges(charges + (int) regained, since + regained * MILLIS_PER_CHARGE);
    }

    public int available(long now) {
        return at(now).charges();
    }

    public boolean isFull(long now) {
        return available(now) >= MAX;
    }

    /** One charge spent breaking a block, or null when the item has none left. */
    public @Nullable BreakerCharges spend(long now) {
        BreakerCharges current = at(now);
        if (current.charges() <= 0) return null;
        long since = current.charges() >= MAX ? now : current.since();
        return new BreakerCharges(current.charges() - 1, since);
    }

    public BreakerCharges restore(int amount, long now) {
        BreakerCharges current = at(now);
        if (amount <= 0) return current;
        int restored = current.charges() + amount;
        return restored >= MAX ? new BreakerCharges(MAX, now) : new BreakerCharges(restored, current.since());
    }
}
