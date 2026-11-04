package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

public enum CenturyTeam {
    BLUE("<9>", "SLICE_OF_BLUEBERRY_CAKE", "Blueberry", "CENTURY_MEMENTO_BLUE", "Century Raffle Blue"),
    YELLOW("<e>", "SLICE_OF_CHEESECAKE", "Lemon Cheesecake", "CENTURY_MEMENTO_YELLOW", "Century Raffle Yellow"),
    GREEN("<a>", "SLICE_OF_GREEN_VELVET_CAKE", "Matcha", "CENTURY_MEMENTO_GREEN", "Century Raffle Green"),
    RED("<c>", "SLICE_OF_RED_VELVET_CAKE", "Red Velvet", "CENTURY_MEMENTO_RED", "Century Raffle Red"),
    PINK("<d>", "SLICE_OF_STRAWBERRY_SHORTCAKE", "Strawberry Shortcake", "CENTURY_MEMENTO_PINK", "Century Raffle Pink");

    private final String colour;
    private final String sliceId;
    private final String flavour;
    private final String mementoId;
    private final String emblemName;

    CenturyTeam(String colour, String sliceId, String flavour, String mementoId, String emblemName) {
        this.colour = colour;
        this.sliceId = sliceId;
        this.flavour = flavour;
        this.mementoId = mementoId;
        this.emblemName = emblemName;
    }

    public String colour() {
        return colour;
    }

    public String display() {
        return colour + "<l>" + name();
    }

    public String flavour() {
        return colour + flavour;
    }

    public ItemType slice() {
        return ItemType.valueOf(sliceId);
    }

    public ItemType memento() {
        return ItemType.valueOf(mementoId);
    }

    public String emblemName() {
        return emblemName;
    }

    public static @Nullable CenturyTeam ofSlice(@Nullable ItemType item) {
        if (item == null) return null;
        for (CenturyTeam team : values()) {
            if (team.sliceId.equals(item.name())) return team;
        }
        return null;
    }

    public static @Nullable CenturyTeam parse(@Nullable String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
