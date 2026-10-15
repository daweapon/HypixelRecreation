package net.swofty.type.skyblockgeneric.experiments;

import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public enum UltrasequencerStake {
    SUPREME("Supreme", "<6>", 7, 3, 3_500, 25, 15, new ClickBonus(2, 4)),
    TRANSCENDENT("Transcendent", "<c>", 7, 4, 5_000, 30, 20, new ClickBonus(2, 4)),
    METAPHYSICAL("Metaphysical", "<d>", 9, 4, 7_000, 40, 40, new ClickBonus(2, 4, 6));

    public static final int XP_NUMBER_CAP = 20;
    public static final int BOARD_FULL_COUNT = 35;
    private static final List<Material> DYES = List.of(Material.WHITE_DYE, Material.LIGHT_BLUE_DYE, Material.BLUE_DYE,
            Material.PURPLE_DYE, Material.MAGENTA_DYE, Material.PINK_DYE, Material.RED_DYE, Material.ORANGE_DYE,
            Material.YELLOW_DYE, Material.LIME_DYE, Material.GREEN_DYE, Material.CYAN_DYE);

    private final String displayName;
    private final String color;
    private final int columns;
    private final int rows;
    private final int xpPerNumber;
    private final int requiredEnchanting;
    private final int restartLevels;
    private final ClickBonus clickBonus;

    UltrasequencerStake(String displayName, String color, int columns, int rows, int xpPerNumber, int requiredEnchanting, int restartLevels, ClickBonus clickBonus) {
        this.displayName = displayName;
        this.color = color;
        this.columns = columns;
        this.rows = rows;
        this.xpPerNumber = xpPerNumber;
        this.requiredEnchanting = requiredEnchanting;
        this.restartLevels = restartLevels;
        this.clickBonus = clickBonus;
    }

    public String displayName() {
        return displayName;
    }

    public String color() {
        return color;
    }

    public int xpPerNumber() {
        return xpPerNumber;
    }

    public int requiredEnchanting() {
        return requiredEnchanting;
    }

    public int restartLevels() {
        return restartLevels;
    }

    public ClickBonus clickBonus() {
        return clickBonus;
    }

    public String gridLabel() {
        return columns + "x" + rows;
    }

    public int maxNumbers() {
        return Math.min(columns * rows, BOARD_FULL_COUNT);
    }

    public long xpFor(int sequence) {
        return (long) Math.min(sequence, XP_NUMBER_CAP) * xpPerNumber;
    }

    public static Material paneFor(int round) {
        return switch ((round - 1) % 6) {
            case 0 -> Material.PURPLE_STAINED_GLASS_PANE;
            case 1 -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            case 2 -> Material.PINK_STAINED_GLASS_PANE;
            case 3 -> Material.RED_STAINED_GLASS_PANE;
            case 4 -> Material.LIME_STAINED_GLASS_PANE;
            default -> Material.ORANGE_STAINED_GLASS_PANE;
        };
    }

    public static Material dyeFor(int number) {
        return DYES.get((number - 1) % DYES.size());
    }

    public List<Integer> slots() {
        List<Integer> slots = new ArrayList<>();
        int firstRow = rows == 3 ? 2 : 1;
        int firstColumn = (9 - columns) / 2;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) slots.add((firstRow + row) * 9 + firstColumn + column);
        }
        return slots;
    }
}
