package dev.daweapon.type.skyblockgeneric.hotm;

import lombok.Getter;
import net.minestom.server.item.Material;

@Getter
public enum HotmAbility {
    MINING_SPEED_BOOST("Mining Speed Boost", 2, Material.GOLDEN_PICKAXE,
            new int[]{10, 15, 20}, new int[]{120, 120, 120}, new double[]{200, 250, 300},
            "Grants <a>+{value}% <6>Mining Speed<7> for <a>{duration}s<7>."),
    PICKOBULUS("Pickobulus", 2, Material.TNT,
            new int[]{0, 0, 0}, new int[]{60, 50, 40}, new double[]{3, 3, 3},
            "Throw your pickaxe to create an explosion mining all ores in a <a>{value}<7> block radius."),
    TUNNEL_VISION("Tunnel Vision", 6, Material.ENDER_EYE,
            new int[]{30, 30, 30}, new int[]{120, 110, 110}, new double[]{30, 40, 50},
            "Increases the chances of triggering rare occurrences by <a>{value}%<7> for <a>{duration}s<7>."),
    MANIAC_MINER("Maniac Miner", 6, Material.NETHERITE_PICKAXE,
            new int[]{25, 30, 35}, new int[]{120, 120, 120}, new double[]{5, 10, 15},
            "Grants <a>+1 Breaking Power<7> and a stack of <6>+{value} Mining Fortune<7> (caps at <6>500<7>) per block broken for <a>{duration}s<7>. Each block broken consumes <b>20 Mana<7>."),
    GEMSTONE_INFUSION("Gemstone Infusion", 10, Material.AMETHYST_SHARD,
            new int[]{20, 25, 30}, new int[]{120, 120, 120}, new double[]{100, 100, 100},
            "Increases the effectiveness of every <d>Gemstone<7> in your drill's Gemstone Slots by <a>{value}%<7> for <a>{duration}s<7>."),
    SHEER_FORCE("Sheer Force", 10, Material.IRON_PICKAXE,
            new int[]{20, 25, 30}, new int[]{120, 120, 120}, new double[]{200, 200, 200},
            "Grants <e>+{value} Mining Spread<7> for <a>{duration}s<7>.");

    public static final int MAX_LEVEL = 3;
    public static final int MANIAC_MINER_FORTUNE_CAP = 500;
    public static final int MANIAC_MINER_MANA_PER_BLOCK = 20;

    private final String displayName;
    private final int unlockTier;
    private final Material icon;
    private final int[] durationSeconds;
    private final int[] cooldownSeconds;
    private final double[] values;
    private final String description;

    HotmAbility(String displayName, int unlockTier, Material icon, int[] durationSeconds, int[] cooldownSeconds,
                double[] values, String description) {
        this.displayName = displayName;
        this.unlockTier = unlockTier;
        this.icon = icon;
        this.durationSeconds = durationSeconds;
        this.cooldownSeconds = cooldownSeconds;
        this.values = values;
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

    public double value(int level) {
        return values[clamp(level)];
    }

    public boolean hasDuration() {
        return durationSeconds[0] > 0;
    }

    private static int clamp(int level) {
        return Math.min(Math.max(level, 1), MAX_LEVEL) - 1;
    }

    public static HotmAbility byId(String id) {
        if (id == null) return null;
        try {
            return valueOf(id);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
