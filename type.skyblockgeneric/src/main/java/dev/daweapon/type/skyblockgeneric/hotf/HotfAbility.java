package dev.daweapon.type.skyblockgeneric.hotf;

import lombok.Getter;
import net.minestom.server.item.Material;

@Getter
public enum HotfAbility {
    AXE_TOSS("Axe Toss", Material.GOLDEN_AXE,
            new int[]{10, 10, 10, 10, 10}, new int[]{120, 115, 110, 105, 100},
            "Grants all Axes the ability to be thrown at trees with <e>Right-Click<7>. Axe Throwing suffers no <a>Sweep<7> penalty for <a>{duration}s<7>."),
    DAMAGE_BOOST("Damage Boost", Material.IRON_AXE,
            new int[]{10, 10, 10, 10, 10}, new int[]{120, 115, 110, 105, 100},
            "Your axe deals <a>double <c>Damage<7> to creatures on <a>Galatea<7> for <a>{duration}s<7>."),
    MANIAC_SLICER("Maniac Slicer", Material.NETHERITE_AXE,
            new int[]{15, 20, 25, 30, 35}, new int[]{60, 59, 58, 57, 56},
            "Consumes all of your current <b>Mana<7> and grants <a>+1 Sweep<7> per <b>100 Mana<7> spent for <a>{duration}s<7>.");

    public static final int MAX_LEVEL = 5;

    private final String displayName;
    private final Material icon;
    private final int[] durationSeconds;
    private final int[] cooldownSeconds;
    private final String description;

    HotfAbility(String displayName, Material icon, int[] durationSeconds, int[] cooldownSeconds, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.durationSeconds = durationSeconds;
        this.cooldownSeconds = cooldownSeconds;
        this.description = description;
    }

    public String perkId() {
        return name().toLowerCase();
    }

    public int duration(int level) {
        return durationSeconds[clamp(level)];
    }

    public int cooldown(int level) {
        return cooldownSeconds[clamp(level)];
    }

    private static int clamp(int level) {
        return Math.min(Math.max(level, 1), MAX_LEVEL) - 1;
    }

    public static HotfAbility byId(String id) {
        if (id == null) return null;
        try {
            return valueOf(id);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
