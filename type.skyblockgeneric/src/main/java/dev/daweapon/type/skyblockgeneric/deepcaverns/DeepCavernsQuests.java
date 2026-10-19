package dev.daweapon.type.skyblockgeneric.deepcaverns;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategory;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public final class DeepCavernsQuests {
    public static final int RHYS_MINING_LEVEL = 12;
    public static final int RHYS_ORES_REQUIRED = 15;
    public static final List<ItemType> RHYS_ORES = List.of(ItemType.ENCHANTED_REDSTONE, ItemType.ENCHANTED_IRON_INGOT,
            ItemType.ENCHANTED_COAL, ItemType.ENCHANTED_EMERALD, ItemType.ENCHANTED_LAPIS_LAZULI, ItemType.ENCHANTED_DIAMOND,
            ItemType.ENCHANTED_GOLD_INGOT);
    public static final int LAPIS_COLLECTION_TIER = 3;
    public static final int LAPIS_UNTOUCHED = 0;
    public static final int LAPIS_ASKED = 1;
    public static final int LAPIS_DONE = 2;
    public static final int REDSTONE_COLLECTION_TIER = 2;
    public static final int REDSTONE_OFFER_NONE = 0;
    public static final int REDSTONE_OFFER_ACCEPTED = 1;
    public static final int REDSTONE_OFFER_DECLINED = 2;
    public static final int[] REDSTONE_TIERS = {128, 256, 512, 1024};

    private DeepCavernsQuests() {
    }

    public static Map<ItemType, Integer> planRhysHandIn(int delivered, ToIntFunction<ItemType> inInventory) {
        Map<ItemType, Integer> plan = new LinkedHashMap<>();
        int remaining = RHYS_ORES_REQUIRED - delivered;
        for (ItemType ore : RHYS_ORES) {
            if (remaining <= 0) break;
            int take = Math.min(remaining, inInventory.applyAsInt(ore));
            if (take > 0) {
                plan.put(ore, take);
                remaining -= take;
            }
        }
        return plan;
    }

    public static int collectionTier(int collected, int... requirements) {
        int tier = 0;
        for (int requirement : requirements) {
            if (collected >= requirement) tier++;
        }
        return tier;
    }

    public static int collectionTier(SkyBlockPlayer player, ItemType type) {
        for (CollectionCategory category : CollectionCategories.getCategories()) {
            for (CollectionCategory.ItemCollection collection : category.getCollections()) {
                if (collection.type() != type) continue;
                int[] requirements = new int[collection.rewards().length];
                for (int i = 0; i < requirements.length; i++) requirements[i] = collection.rewards()[i].requirement();
                return collectionTier(player.getCollection().get(type), requirements);
            }
        }
        return 0;
    }

    public static int redstoneSlots(int tier) {
        return Math.max(0, Math.min(tier, REDSTONE_TIERS.length));
    }

    public static boolean redstoneFinished(int tier) {
        return tier >= REDSTONE_TIERS.length;
    }

    public static int redstoneCost(int tier) {
        return REDSTONE_TIERS[Math.min(tier, REDSTONE_TIERS.length - 1)];
    }

    public static boolean miningLevelReached(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.MINING) >= RHYS_MINING_LEVEL;
    }

    public static int rhysOres(SkyBlockPlayer player) {
        return value(player, SkyBlockDataHandler.Data.RHYS_ORES_DELIVERED);
    }

    public static void setRhysOres(SkyBlockPlayer player, int delivered) {
        set(player, SkyBlockDataHandler.Data.RHYS_ORES_DELIVERED, Math.min(delivered, RHYS_ORES_REQUIRED));
    }

    public static boolean rhysComplete(SkyBlockPlayer player) {
        return rhysOres(player) >= RHYS_ORES_REQUIRED;
    }

    public static int lapisMinerState(SkyBlockPlayer player) {
        return value(player, SkyBlockDataHandler.Data.LAPIS_MINER_STATE);
    }

    public static void setLapisMinerState(SkyBlockPlayer player, int state) {
        set(player, SkyBlockDataHandler.Data.LAPIS_MINER_STATE, state);
    }

    public static int redstoneTier(SkyBlockPlayer player) {
        return value(player, SkyBlockDataHandler.Data.REDSTONE_MINER_TIER);
    }

    public static void setRedstoneTier(SkyBlockPlayer player, int tier) {
        set(player, SkyBlockDataHandler.Data.REDSTONE_MINER_TIER, Math.min(tier, REDSTONE_TIERS.length));
    }

    public static int redstoneOffer(SkyBlockPlayer player) {
        return value(player, SkyBlockDataHandler.Data.REDSTONE_MINER_ACCEPTED);
    }

    public static void setRedstoneOffer(SkyBlockPlayer player, int offer) {
        set(player, SkyBlockDataHandler.Data.REDSTONE_MINER_ACCEPTED, offer);
    }

    public static void maxOut(SkyBlockPlayer player) {
        setRhysOres(player, RHYS_ORES_REQUIRED);
        setLapisMinerState(player, LAPIS_DONE);
        setRedstoneTier(player, REDSTONE_TIERS.length);
        setRedstoneOffer(player, REDSTONE_OFFER_ACCEPTED);
    }

    private static int value(SkyBlockPlayer player, SkyBlockDataHandler.Data data) {
        if (player.getSkyblockDataHandler() == null) return 0;
        return player.getSkyblockDataHandler().get(data, DatapointInteger.class).getValue();
    }

    private static void set(SkyBlockPlayer player, SkyBlockDataHandler.Data data, int value) {
        player.getSkyblockDataHandler().get(data, DatapointInteger.class).setValue(value);
    }
}
