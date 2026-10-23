package dev.daweapon.type.skyblockgeneric.rewardbundle;

import net.minestom.server.item.Material;

import java.time.LocalDate;

public enum BundleSeason {
    EASTER("Easter", "Bunny Man", Material.EGG),
    SUMMER("Summer", "Summer Sloth", Material.SUNFLOWER),
    HALLOWEEN("Halloween", "Spooky Man", Material.JACK_O_LANTERN),
    HOLIDAYS("Holidays", "Gingerbread Man", Material.SPRUCE_SAPLING);

    private final String label;
    private final String npcName;
    private final Material icon;

    BundleSeason(String label, String npcName, Material icon) {
        this.label = label;
        this.npcName = npcName;
        this.icon = icon;
    }

    public String label() {
        return label;
    }

    public String npcName() {
        return npcName;
    }

    public Material icon() {
        return icon;
    }

    public BundleSeason next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public BundleSeason previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public static BundleSeason current(LocalDate date) {
        return switch (date.getMonthValue()) {
            case 4, 5 -> EASTER;
            case 6, 7, 8 -> SUMMER;
            case 10 -> HALLOWEEN;
            case 12 -> HOLIDAYS;
            default -> null;
        };
    }
}
