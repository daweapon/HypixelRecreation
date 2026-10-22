package dev.daweapon.type.skyblockgeneric.beacon;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public record BeaconData(int tier, int x, int y, int z, long poweredUntil, long scorchedUntil,
                         @Nullable BeaconStat stat, Set<BeaconEffect> effects) {
    public static final long POWER_MILLIS = 48L * 60 * 60 * 1000;
    public static final double SCORCHED_STAT_MULTIPLIER = 1.2;
    public static final double SCORCHED_MINION_BONUS_PERCENT = 1;
    public static final double MINION_PERCENT_PER_TIER = 2;
    public static final String DATABASE_KEY = "beacon";

    public BeaconData {
        effects = Collections.unmodifiableSet(effects.isEmpty() ? EnumSet.noneOf(BeaconEffect.class) : EnumSet.copyOf(effects));
    }

    public static BeaconData placed(int tier, int x, int y, int z) {
        return new BeaconData(tier, x, y, z, 0, 0, null, Set.of());
    }

    public boolean isPowered(long now) {
        return poweredUntil > now;
    }

    public boolean isScorched(long now) {
        return isPowered(now) && scorchedUntil > now;
    }

    public long remainingMillis(long now) {
        return Math.max(0, poweredUntil - now);
    }

    public BeaconData withCrystals(int amount, boolean scorched, long now) {
        long added = POWER_MILLIS * amount;
        long newPowered = Math.max(poweredUntil, now) + added;
        long newScorched = scorched ? Math.max(scorchedUntil, now) + added : scorchedUntil;
        return new BeaconData(tier, x, y, z, newPowered, newScorched, stat, effects);
    }

    public BeaconData withStat(@Nullable BeaconStat chosen) {
        return new BeaconData(tier, x, y, z, poweredUntil, scorchedUntil, chosen, effects);
    }

    public BeaconData withEffectToggled(BeaconEffect effect) {
        Set<BeaconEffect> updated = effects.isEmpty() ? EnumSet.noneOf(BeaconEffect.class) : EnumSet.copyOf(effects);
        if (!updated.remove(effect)) updated.add(effect);
        return new BeaconData(tier, x, y, z, poweredUntil, scorchedUntil, stat, updated);
    }

    public double minionSpeedPercent(long now) {
        if (!isPowered(now)) return 0;
        return MINION_PERCENT_PER_TIER * tier + (isScorched(now) ? SCORCHED_MINION_BONUS_PERCENT : 0);
    }

    public double profileStatValue(long now) {
        if (!isPowered(now) || stat == null) return 0;
        double base = stat.valueAtTier(tier);
        return isScorched(now) ? base * SCORCHED_STAT_MULTIPLIER : base;
    }

    public Set<BeaconEffect> activeEffects(long now) {
        if (!isPowered(now)) return Set.of();
        return effects.stream().filter(effect -> effect.unlockedAt(tier)).collect(Collectors.toCollection(() -> EnumSet.noneOf(BeaconEffect.class)));
    }

    public boolean isAt(int blockX, int blockY, int blockZ) {
        return x == blockX && y == blockY && z == blockZ;
    }

    public String serialize() {
        String effectNames = effects.stream().map(Enum::name).sorted().collect(Collectors.joining(","));
        return String.join("|", String.valueOf(tier), String.valueOf(x), String.valueOf(y), String.valueOf(z),
                String.valueOf(poweredUntil), String.valueOf(scorchedUntil), stat == null ? "" : stat.name(), effectNames);
    }

    public static @Nullable BeaconData deserialize(@Nullable String raw) {
        if (raw == null || raw.isBlank()) return null;
        String[] parts = raw.split("\\|", -1);
        if (parts.length < 8) return null;
        try {
            Set<BeaconEffect> effects = EnumSet.noneOf(BeaconEffect.class);
            Arrays.stream(parts[7].split(",")).filter(name -> !name.isBlank()).forEach(name -> {
                try {
                    effects.add(BeaconEffect.valueOf(name));
                } catch (IllegalArgumentException ignored) {
                }
            });
            return new BeaconData(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]), Long.parseLong(parts[4]), Long.parseLong(parts[5]),
                    BeaconStat.parse(parts[6]), effects);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
