package net.swofty.type.skyblockgeneric.experiments;

import net.minestom.server.item.Material;

import java.util.List;

public enum ChronomatronStake {
    HIGH("High", 3, 1_500, 20, 5, new ClickBonus(5, 9)),
    GRAND("Grand", 5, 2_500, 25, 10, new ClickBonus(5, 9)),
    SUPREME("Supreme", 7, 3_500, 30, 15, new ClickBonus(5, 9)),
    TRANSCENDENT("Transcendent", 8, 4_500, 35, 20, new ClickBonus(5, 9)),
    METAPHYSICAL("Metaphysical", 10, 6_000, 40, 40, new ClickBonus(5, 9, 12));

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
    private final int colors;
    private final int xpPerNote;
    private final int requiredEnchanting;
    private final int restartLevels;
    private final ClickBonus clickBonus;

    ChronomatronStake(String displayName, int colors, int xpPerNote, int requiredEnchanting, int restartLevels, ClickBonus clickBonus) {
        this.displayName = displayName;
        this.colors = colors;
        this.xpPerNote = xpPerNote;
        this.requiredEnchanting = requiredEnchanting;
        this.restartLevels = restartLevels;
        this.clickBonus = clickBonus;
    }

    public String displayName() {
        return displayName;
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

    public int[] slots() {
        if (colors <= 7) {
            int start = 27 + (9 - colors) / 2;
            int[] slots = new int[colors];
            for (int i = 0; i < colors; i++) slots[i] = start + i;
            return slots;
        }
        int top = colors / 2;
        int bottom = colors - top;
        int[] slots = new int[colors];
        for (int i = 0; i < top; i++) slots[i] = 19 + (9 - top) / 2 + i;
        for (int i = 0; i < bottom; i++) slots[top + i] = 28 + (9 - bottom) / 2 + i;
        return slots;
    }
}
