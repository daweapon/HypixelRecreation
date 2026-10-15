package net.swofty.type.skyblockgeneric.experiments;

import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public enum ChronomatronStake {
    HIGH("High", "<a>", 3, 1_500, 20, 5, new ClickBonus(2, 6)),
    GRAND("Grand", "<e>", 5, 2_500, 25, 10, new ClickBonus(2, 6)),
    SUPREME("Supreme", "<6>", 7, 3_500, 30, 15, new ClickBonus(2, 6)),
    TRANSCENDENT("Transcendent", "<c>", 8, 4_500, 35, 20, new ClickBonus(2, 6)),
    METAPHYSICAL("Metaphysical", "<d>", 10, 6_000, 40, 40, new ClickBonus(2, 6, 9));

    public static final int XP_NOTE_CAP = 15;
    public static final int MAX_ROUNDS = 49;
    private static final List<Material> GLASS = List.of(Material.RED_STAINED_GLASS, Material.BLUE_STAINED_GLASS,
            Material.LIME_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS,
            Material.PINK_STAINED_GLASS, Material.GREEN_STAINED_GLASS, Material.CYAN_STAINED_GLASS,
            Material.ORANGE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS);
    private static final List<Material> PANES = List.of(Material.RED_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE,
            Material.LIME_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.PINK_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE, Material.CYAN_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE);

    private final String displayName;
    private final String color;
    private final int colors;
    private final int xpPerNote;
    private final int requiredEnchanting;
    private final int restartLevels;
    private final ClickBonus clickBonus;

    ChronomatronStake(String displayName, String color, int colors, int xpPerNote, int requiredEnchanting, int restartLevels, ClickBonus clickBonus) {
        this.displayName = displayName;
        this.color = color;
        this.colors = colors;
        this.xpPerNote = xpPerNote;
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

    public int colors() {
        return colors;
    }

    public int xpPerNote() {
        return xpPerNote;
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

    public Material litMaterial(int color) {
        return GLASS.get(color);
    }

    public Material dimMaterial(int color) {
        return PANES.get(color);
    }

    public long xpFor(int chain) {
        return (long) Math.min(chain, XP_NOTE_CAP) * xpPerNote;
    }

    public List<int[]> tileSlots() {
        int[] groups = switch (colors) {
            case 7 -> new int[]{4, 3};
            case 8 -> new int[]{4, 4};
            case 10 -> new int[]{5, 5};
            default -> new int[]{colors};
        };
        List<int[]> tiles = new ArrayList<>();
        for (int group = 0; group < groups.length; group++) {
            int topRow = 1 + group * 2;
            int firstColumn = 4 - groups[group] / 2;
            for (int i = 0; i < groups[group]; i++) {
                int slot = topRow * 9 + firstColumn + i;
                tiles.add(new int[]{slot, slot + 9});
            }
        }
        return tiles;
    }
}
