package dev.daweapon.type.skyblockgeneric.garden.pest;

import org.jetbrains.annotations.Nullable;

public enum PestNozzle {
    BASIC("Basic", "<a>", "SPRAYONATOR", 25, 1.5, 1, 30),
    JUICY("Juicy", "<9>", "JUICY_SPRAYONATOR", 50, 2, 3, 45),
    SALTY("Salty", "<5>", "SALTY_SPRAYONATOR", 75, 3, 5, 60);

    private final String displayName;
    private final String color;
    private final String sprayonator;
    private final int bonusChance;
    private final double spawnMultiplier;
    private final int materialCost;
    private final int minutes;

    PestNozzle(String displayName, String color, String sprayonator, int bonusChance, double spawnMultiplier, int materialCost, int minutes) {
        this.displayName = displayName;
        this.color = color;
        this.sprayonator = sprayonator;
        this.bonusChance = bonusChance;
        this.spawnMultiplier = spawnMultiplier;
        this.materialCost = materialCost;
        this.minutes = minutes;
    }

    public String displayName() {
        return displayName;
    }

    public String colored() {
        return color + displayName;
    }

    public String sprayonator() {
        return sprayonator;
    }

    public int bonusChance() {
        return bonusChance;
    }

    public double spawnMultiplier() {
        return spawnMultiplier;
    }

    public int materialCost() {
        return materialCost;
    }

    public long durationMillis() {
        return minutes * 60_000L;
    }

    public int minutes() {
        return minutes;
    }

    public static @Nullable PestNozzle ofSprayonator(String itemId) {
        for (PestNozzle nozzle : values()) if (nozzle.sprayonator.equals(itemId)) return nozzle;
        return null;
    }

    public static PestNozzle parse(@Nullable String name) {
        if (name != null) {
            for (PestNozzle nozzle : values()) if (nozzle.name().equalsIgnoreCase(name)) return nozzle;
        }
        return BASIC;
    }

    public PestNozzle cycle(PestNozzle best) {
        PestNozzle[] all = values();
        PestNozzle next = all[(ordinal() + 1) % all.length];
        return next.ordinal() > best.ordinal() ? BASIC : next;
    }

    public PestNozzle clamp(PestNozzle best) {
        return ordinal() > best.ordinal() ? best : this;
    }
}
