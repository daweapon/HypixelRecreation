package dev.daweapon.type.skyblockgeneric.accessory.handler;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Small numeric accessory bonuses read by other systems: XP orbs, Fishing Hotspots, minions and commissions. */
public final class UtilityAccessories {
    private static final double EXPERIENCE_ARTIFACT_MULTIPLIER = 1.25;
    private static final int KING_TALISMAN_HOTM_XP = 10;

    private UtilityAccessories() {
    }

    public static double experienceOrbMultiplier(SkyBlockPlayer player) {
        return AccessoryOwnership.owns(player, ItemType.EXPERIENCE_ARTIFACT) ? EXPERIENCE_ARTIFACT_MULTIPLIER : 1;
    }

    public static ItemStatistics hotspotBuffs(SkyBlockPlayer player) {
        if (AccessoryOwnership.owns(player, ItemType.HOTSPOT_ARTIFACT)) return hotspot(10, 1, 0.5);
        if (AccessoryOwnership.owns(player, ItemType.HOTSPOT_RING)) return hotspot(5, 0.5, 0);
        if (AccessoryOwnership.owns(player, ItemType.HOTSPOT_TALISMAN)) return hotspot(2.5, 0, 0);
        return ItemStatistics.empty();
    }

    public static int commissionHotmXp(SkyBlockPlayer player) {
        return AccessoryOwnership.owns(player, ItemType.KING_TALISMAN) ? KING_TALISMAN_HOTM_XP : 0;
    }

    private static ItemStatistics hotspot(double fishingSpeed, double seaCreatureChance, double treasureChance) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.FISHING_SPEED, fishingSpeed)
                .withBase(ItemStatistic.SEA_CREATURE_CHANCE, seaCreatureChance)
                .withBase(ItemStatistic.TREASURE_CHANCE, treasureChance)
                .build();
    }
}
