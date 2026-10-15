package dev.daweapon.dungeons.catacombs;

public enum CatacombsDungeonSize {
    FOUR_BY_FOUR(4, 4),
    FOUR_BY_FIVE(4, 5),
    FIVE_BY_FIVE(5, 5),
    SIX_BY_FIVE(6, 5),
    SIX_BY_SIX(6, 6);

    private final int width;
    private final int height;

    CatacombsDungeonSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
