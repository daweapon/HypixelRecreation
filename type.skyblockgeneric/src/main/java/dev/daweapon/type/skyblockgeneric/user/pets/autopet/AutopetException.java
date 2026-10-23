package dev.daweapon.type.skyblockgeneric.user.pets.autopet;

import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

public enum AutopetException {
    IN_ISLAND("Except if in island", AutopetParam.ISLAND, Material.GRASS_BLOCK, null),
    NOT_IN_ISLAND("Except if NOT in island", AutopetParam.ISLAND, Material.BARRIER, null),
    PET_EQUIPPED("Except if already has pet equipped", AutopetParam.PET, Material.BONE, null),
    DURING_EVENT("Except if during event", AutopetParam.EVENT, Material.CLOCK, null),
    SLAYER_ACTIVE("Except if has active slayer quest", AutopetParam.NONE, Material.BLAZE_POWDER, null),
    SLAYER_BOSS_SPAWNED("Except if you have a slayer quest with a spawned boss", AutopetParam.NONE, Material.ZOMBIE_HEAD, null),
    DAY("Except if it is day", AutopetParam.NONE, Material.SUNFLOWER, "The wiki does not say which day it means");

    private final String label;
    private final AutopetParam param;
    private final Material icon;
    private final @Nullable String unavailable;

    AutopetException(String label, AutopetParam param, Material icon, @Nullable String unavailable) {
        this.label = label;
        this.param = param;
        this.icon = icon;
        this.unavailable = unavailable;
    }

    public String label() {
        return label;
    }

    public AutopetParam param() {
        return param;
    }

    public Material icon() {
        return icon;
    }

    public @Nullable String unavailable() {
        return unavailable;
    }

    public boolean isAvailable() {
        return unavailable == null;
    }
}
