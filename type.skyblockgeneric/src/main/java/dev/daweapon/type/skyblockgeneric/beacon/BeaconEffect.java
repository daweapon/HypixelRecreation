package dev.daweapon.type.skyblockgeneric.beacon;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import net.minestom.server.item.Material;
import net.minestom.server.potion.PotionEffect;

public enum BeaconEffect {
    SPEED("<f>Speed", Material.SUGAR, 1, null, 0, ItemStatistic.SPEED, 50),
    JUMP_BOOST("<a>Jump Boost II", Material.RABBIT_FOOT, 2, PotionEffect.JUMP_BOOST, 1, null, 0),
    HASTE("<e>Haste II", Material.GOLDEN_PICKAXE, 3, PotionEffect.HASTE, 1, null, 0),
    REGENERATION("<c>Regeneration II", Material.GLISTERING_MELON_SLICE, 4, PotionEffect.REGENERATION, 1, null, 0),
    STRENGTH("<c>+40 Strength", Material.BLAZE_POWDER, 5, null, 0, ItemStatistic.STRENGTH, 40),
    DEFENSE("<a>+40 Defense", Material.IRON_CHESTPLATE, 5, null, 0, ItemStatistic.DEFENSE, 40);

    private final String label;
    private final Material icon;
    private final int unlockTier;
    private final PotionEffect potion;
    private final int amplifier;
    private final ItemStatistic statistic;
    private final double statValue;

    BeaconEffect(String label, Material icon, int unlockTier, PotionEffect potion, int amplifier,
                 ItemStatistic statistic, double statValue) {
        this.label = label;
        this.icon = icon;
        this.unlockTier = unlockTier;
        this.potion = potion;
        this.amplifier = amplifier;
        this.statistic = statistic;
        this.statValue = statValue;
    }

    public String label() {
        return label;
    }

    public Material icon() {
        return icon;
    }

    public int unlockTier() {
        return unlockTier;
    }

    public PotionEffect potion() {
        return potion;
    }

    public int amplifier() {
        return amplifier;
    }

    public ItemStatistic statistic() {
        return statistic;
    }

    public double statValue() {
        return statValue;
    }

    public boolean unlockedAt(int tier) {
        return tier >= unlockTier;
    }
}
