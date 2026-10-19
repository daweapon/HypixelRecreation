package dev.daweapon.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.item.Material;

import java.util.List;
import java.util.function.Supplier;

public enum ToyBoxCategory {
    MINING("Mining", Material.DIAMOND_PICKAXE, "Heart of the Mountain, the Forge, drills and the Crystal Hollows.", ToyBoxButtons::mining),
    GARDEN("Garden", Material.SUNFLOWER, "Visitors, pests, the greenhouse, Garden levels and contest rewards.", ToyBoxButtons::garden),
    EXPERIMENTATION("Experimentation", Material.ENCHANTING_TABLE, "Enchanting experience, bottles and the Experimentation Table.", ToyBoxButtons::experimentation),
    CURRENCIES("Currencies and Essence", Material.EMERALD, "Gems, Whispers, Shards and every kind of Essence.", ToyBoxButtons::currencies),
    HOPPITY("Hoppity's Hunt", Material.RABBIT_FOOT, "Chocolate, rabbits, prestige, the Time Tower and the Rabbit Hitman.", ToyBoxButtons::hoppity),
    DUNGEONS("Dungeons", Material.WITHER_SKELETON_SKULL, "Catacombs experience and other dungeon progression.", ToyBoxButtons::dungeons);

    private final String title;
    private final Material icon;
    private final String description;
    private final Supplier<List<ToyBoxButton>> buttons;

    ToyBoxCategory(String title, Material icon, String description, Supplier<List<ToyBoxButton>> buttons) {
        this.title = title;
        this.icon = icon;
        this.description = description;
        this.buttons = buttons;
    }

    public String title() {
        return title;
    }

    public Material icon() {
        return icon;
    }

    public String description() {
        return description;
    }

    public List<ToyBoxButton> buttons() {
        return buttons.get();
    }
}
