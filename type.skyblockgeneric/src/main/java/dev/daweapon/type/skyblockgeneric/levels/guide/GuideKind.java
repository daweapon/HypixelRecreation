package dev.daweapon.type.skyblockgeneric.levels.guide;

import net.minestom.server.item.Material;

public enum GuideKind {
    SKILL(Material.DIAMOND_SWORD),
    SLAYER(Material.ROTTEN_FLESH),
    ACCESSORY(Material.PLAYER_HEAD),
    PET(Material.BONE),
    COLLECTION(Material.PAINTING),
    MINION(Material.COBBLESTONE),
    MOB_TYPE(Material.SPAWNER),
    MUSEUM(Material.GOLD_BLOCK),
    OTHER(Material.PAPER);

    private final Material fallbackIcon;

    GuideKind(Material fallbackIcon) {
        this.fallbackIcon = fallbackIcon;
    }

    public Material fallbackIcon() {
        return fallbackIcon;
    }
}
