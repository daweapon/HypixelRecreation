package net.swofty.type.skyblockgeneric.experiments;

import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public enum UltrasequencerStake {
    SUPREME("Supreme", 7, 3, 3_500, 25, 15, new ClickBonus(5, 7)),
    TRANSCENDENT("Transcendent", 7, 4, 5_000, 30, 20, new ClickBonus(5, 7)),
    METAPHYSICAL("Metaphysical", 9, 4, 7_000, 40, 40, new ClickBonus(5, 7, 9));

    public static final int XP_NUMBER_CAP = 20;
    public static final int BOARD_FULL_COUNT = 35;
    private static final List<Material> DYES = List.of(Material.WHITE_DYE, Material.LIGHT_BLUE_DYE, Material.BLUE_DYE,
            Material.PURPLE_DYE, Material.MAGENTA_DYE, Material.PINK_DYE, Material.RED_DYE, Material.ORANGE_DYE,
            Material.YELLOW_DYE, Material.LIME_DYE, Material.GREEN_DYE, Material.CYAN_DYE);

    private final String displayName;
    private final int columns;
    private final int rows;
    private final int xpPerNumber;
    private final int requiredEnchanting;
    private final int restartLevels;
    private final ClickBonus clickBonus;

    UltrasequencerStake(String displayName, int columns, int rows, int xpPerNumber, int requiredEnchanting, int restartLevels, ClickBonus clickBonus) {
        this.displayName = displayName;
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

    public int maxNumbers() {
        return Math.min(columns * rows, BOARD_FULL_COUNT);
    }

    public long xpFor(int sequence) {
        return (long) Math.min(sequence, XP_NUMBER_CAP) * xpPerNumber;
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
