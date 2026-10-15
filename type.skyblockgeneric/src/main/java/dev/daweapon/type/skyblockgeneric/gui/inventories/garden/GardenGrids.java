package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;

import java.util.List;
import java.util.OptionalInt;

final class GardenGrids {
    static final int MAX_SPEED_CAP = 1_000_000;
    static final String DEFAULT_PRESET = "Default";
    static final int BARN_SLOT = 22;

    record Preset(int slot, String name, Material icon) {
    }

    static final List<GardenCrop> CROP_ORDER = List.of(
            GardenCrop.WHEAT, GardenCrop.CARROT, GardenCrop.POTATO, GardenCrop.PUMPKIN, GardenCrop.SUGAR_CANE,
            GardenCrop.MELON, GardenCrop.CACTUS, GardenCrop.COCOA_BEANS, GardenCrop.MUSHROOM, GardenCrop.NETHER_WART,
            GardenCrop.MOONFLOWER, GardenCrop.SUNFLOWER, GardenCrop.WILD_ROSE);

    static final List<Integer> CROP_SLOTS = List.of(11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 30, 31, 32);

    static final int[][] PLOT_GRID = dev.daweapon.type.skyblockgeneric.garden.GardenLayout.PLOT_GRID;

    static final List<Preset> PRESETS = List.of(
            new Preset(10, DEFAULT_PRESET, Material.GRASS_BLOCK),
            new Preset(11, "Wheat", Material.WHEAT),
            new Preset(12, "Carrot", Material.CARROT),
            new Preset(13, "Potato", Material.POTATO),
            new Preset(14, "Pumpkin", Material.CARVED_PUMPKIN),
            new Preset(15, "Sugar Cane", Material.SUGAR_CANE),
            new Preset(16, "Melon", Material.MELON_SLICE),
            new Preset(19, "Cactus", Material.CACTUS),
            new Preset(20, "Cocoa Beans", Material.COCOA_BEANS),
            new Preset(21, "Mushroom", Material.RED_MUSHROOM),
            new Preset(22, "Nether Wart", Material.NETHER_WART),
            new Preset(23, "Sunflower", Material.SUNFLOWER),
            new Preset(24, "Wild Rose", Material.ROSE_BUSH),
            new Preset(25, "Moonflower", Material.BLUE_ORCHID),
            new Preset(28, "MEGA FARM MIDDLE", Material.DIAMOND_BLOCK),
            new Preset(29, "MEGA FARM CORNER", Material.MOSS_BLOCK),
            new Preset(30, "NEON FARM CORNER", Material.COAL_BLOCK),
            new Preset(31, "NEON FARM MIDDLE", Material.RED_NETHER_BRICKS));

    private GardenGrids() {
    }

    static int plotSlot(int row, int column) {
        return row * 9 + column + 2;
    }

    static OptionalInt plotAt(int slot) {
        for (int row = 0; row < PLOT_GRID.length; row++) {
            for (int column = 0; column < PLOT_GRID[row].length; column++) {
                if (plotSlot(row, column) == slot && PLOT_GRID[row][column] > 0) {
                    return OptionalInt.of(PLOT_GRID[row][column]);
                }
            }
        }
        return OptionalInt.empty();
    }

    static int slotOfPlot(int plot) {
        for (int row = 0; row < PLOT_GRID.length; row++) {
            for (int column = 0; column < PLOT_GRID[row].length; column++) {
                if (PLOT_GRID[row][column] == plot) {
                    return plotSlot(row, column);
                }
            }
        }
        return -1;
    }

    static int bundleCount(int compost, int perBundle) {
        return perBundle > 0 && compost >= perBundle && compost % perBundle == 0 ? compost / perBundle : 0;
    }

    static OptionalInt parseSpeedCap(String input) {
        if (input == null) {
            return OptionalInt.empty();
        }
        try {
            int cap = Integer.parseInt(input.trim().replace(",", ""));
            return cap >= 1 && cap <= MAX_SPEED_CAP ? OptionalInt.of(cap) : OptionalInt.empty();
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    static Material presetIcon(String name) {
        return PRESETS.stream()
                .filter(preset -> preset.name().equals(name))
                .map(Preset::icon)
                .findFirst()
                .orElse(Material.LIME_STAINED_GLASS_PANE);
    }

    static boolean hasPreset(String name) {
        return name != null && !name.equals(DEFAULT_PRESET) && PRESETS.stream().anyMatch(preset -> preset.name().equals(name));
    }
}
