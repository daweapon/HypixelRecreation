package net.swofty.type.skyblockgeneric.accessory;

import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Accessory Bag size, as Hypixel counts it since July 2026: 9 slots from the start, +6 per Redstone
 * collection tier II to IX (48), +2 per Jacobus purchase (198), +1 per Accessory Size attribute level
 * (10), plus the Community Center account upgrade (12) and the Redstone Miner (4), which aren't built.
 */
public final class AccessoryBagSlots {
    public static final int BASE = 9;
    public static final int PER_REDSTONE_UPGRADE = 6;
    public static final int PER_JACOBUS_UPGRADE = 2;
    public static final int MAX_JACOBUS_UPGRADES = 99;
    public static final int MAX_ACCESSORY_SIZE = 10;
    public static final int COMMUNITY_CENTER_MAX = 12;
    public static final int REDSTONE_MINER = 4;
    public static final List<CustomCollectionAward> REDSTONE_UPGRADES = List.of(
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_2,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_3,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_4,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_5,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_6,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_7,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_8,
            CustomCollectionAward.ACCESSORY_BAG_UPGRADE_9);
    public static final int MAXIMUM = BASE + REDSTONE_UPGRADES.size() * PER_REDSTONE_UPGRADE
            + MAX_JACOBUS_UPGRADES * PER_JACOBUS_UPGRADE + MAX_ACCESSORY_SIZE + COMMUNITY_CENTER_MAX + REDSTONE_MINER;
    private static final AttributeId ACCESSORY_SIZE = AttributeId.parse("R34");

    private AccessoryBagSlots() {
    }

    public static int total(SkyBlockPlayer player) {
        return BASE + redstone(player) + jacobus(player) + accessorySize(player) + communityCenter(player) + redstoneMiner(player);
    }

    public static int redstone(SkyBlockPlayer player) {
        return (int) REDSTONE_UPGRADES.stream().filter(player::hasCustomCollectionAward).count() * PER_REDSTONE_UPGRADE;
    }

    public static int jacobus(SkyBlockPlayer player) {
        return PER_JACOBUS_UPGRADE * player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.JACOBUS_UPGRADES, DatapointInteger.class).getValue();
    }

    public static int accessorySize(SkyBlockPlayer player) {
        return (int) Math.min(MAX_ACCESSORY_SIZE, AttributeEffectService.value(player.getHuntingData(), ACCESSORY_SIZE));
    }

    public static int communityCenter(SkyBlockPlayer player) {
        return 0;
    }

    public static int redstoneMiner(SkyBlockPlayer player) {
        return 0;
    }

    /** The next Redstone upgrade still to unlock, or null once all eight are owned. */
    public static CustomCollectionAward nextRedstoneUpgrade(SkyBlockPlayer player) {
        return REDSTONE_UPGRADES.stream().filter(award -> !player.hasCustomCollectionAward(award)).findFirst().orElse(null);
    }
}
