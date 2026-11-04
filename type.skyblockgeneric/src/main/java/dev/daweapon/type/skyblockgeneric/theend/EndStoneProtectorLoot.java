package dev.daweapon.type.skyblockgeneric.theend;

import dev.daweapon.commons.skyblock.item.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * End Stone Protector loot from the wiki: Quality = placement base + Zealots contributed (max 100) + 50 x damage
 * share of first place. Placement guarantees End Stone, Crystal Fragments and a Rose; uniques roll in order,
 * once per boss, and cost their Quality when won.
 */
public final class EndStoneProtectorLoot {
    public static final double DAMAGE_SHARE_QUALITY = 50;
    public static final int SHARD_QUALITY = 240;
    public static final double PEARLESCENT_DYE_CHANCE = 0.001;

    public record Unique(String itemId, @Nullable Rarity rarity, double chancePercent, int quality) {
        public String key() {
            return rarity == null ? itemId : itemId + ":" + rarity.name();
        }
    }

    public static final List<Unique> UNIQUES = List.of(
            new Unique("PET_ITEM_TIER_BOOST_DROP", null, 0.2, 250),
            new Unique("GOLEM_PET", Rarity.LEGENDARY, 0.6, 235),
            new Unique("GOLEM_PET", Rarity.EPIC, 2, 220));

    public record Reward(String itemId, int amount, @Nullable Rarity rarity, boolean rare) {
    }

    @FunctionalInterface
    public interface Roller {
        boolean roll(String itemId, double chancePercent);
    }

    private static final int[] TOP_BASE = {200, 175, 150, 125, 110, 100, 100, 100, 90, 90, 80, 80};
    private static final int[] TOP_END_STONE = {10, 7, 4, 2, 2, 2, 2, 2};
    private static final int[] TOP_FRAGMENTS = {2, 1, 1, 1, 1};

    private EndStoneProtectorLoot() {
    }

    public static int baseQuality(int rank, double damage) {
        if (rank <= TOP_BASE.length) return TOP_BASE[rank - 1];
        return damage > 1 ? 70 : 10;
    }

    public static int quality(int rank, double damage, double topDamage, int zealots) {
        double share = topDamage <= 0 ? 0 : Math.min(1, damage / topDamage);
        return baseQuality(rank, damage) + Math.clamp(zealots, 0, ProtectorProgress.MAX_QUALITY_ZEALOTS)
                + (int) Math.round(DAMAGE_SHARE_QUALITY * share);
    }

    public static int enchantedEndStone(int rank) {
        return rank <= TOP_END_STONE.length ? TOP_END_STONE[rank - 1] : 1;
    }

    public static int crystalFragments(int rank) {
        return rank <= TOP_FRAGMENTS.length ? TOP_FRAGMENTS[rank - 1] : 0;
    }

    public static List<Reward> plan(int rank, double damage, double topDamage, int zealots,
                                    Set<String> claimedUniques, Roller roller) {
        List<Reward> rewards = new ArrayList<>();
        rewards.add(new Reward("ENCHANTED_END_STONE", enchantedEndStone(rank), null, false));
        int fragments = crystalFragments(rank);
        if (fragments > 0) rewards.add(new Reward("CRYSTAL_FRAGMENT", fragments, null, false));
        rewards.add(new Reward("ENDSTONE_ROSE", 1, null, false));
        int quality = quality(rank, damage, topDamage, zealots);
        for (Unique unique : UNIQUES) {
            if (claimedUniques.contains(unique.key()) || quality < unique.quality()) continue;
            if (!roller.roll(unique.itemId(), unique.chancePercent())) continue;
            claimedUniques.add(unique.key());
            quality -= unique.quality();
            rewards.add(new Reward(unique.itemId(), 1, unique.rarity(), true));
        }
        if (quality >= SHARD_QUALITY) rewards.add(new Reward("SHARD_END_STONE_PROTECTOR", 1, null, false));
        if (roller.roll("DYE_PEARLESCENT", PEARLESCENT_DYE_CHANCE)) {
            rewards.add(new Reward("DYE_PEARLESCENT", 1, null, true));
        }
        return rewards;
    }
}
