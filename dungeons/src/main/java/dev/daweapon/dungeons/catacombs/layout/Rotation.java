package dev.daweapon.dungeons.catacombs.layout;

public enum Rotation {
    DEG_0,
    DEG_90,
    DEG_180,
    DEG_270;

    public int quarterTurns() {
        return ordinal();
    }
}
