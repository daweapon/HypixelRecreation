package net.swofty.type.skyblockgeneric.achievement;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMinionData;
import net.swofty.type.skyblockgeneric.minion.IslandMinionData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.island.IslandMembers;
import net.swofty.type.skyblockgeneric.user.island.SkyBlockIsland;

import java.util.Map;

public final class SkyBlockItemAchievements {
    private static final Map<ItemType, String> CRAFTED = Map.ofEntries(
            Map.entry(ItemType.BAIT_RING, "skyblock.baited"),
            Map.entry(ItemType.SPEED_ARTIFACT, "skyblock.gotta_go_fast"),
            Map.entry(ItemType.SEA_CREATURE_ARTIFACT, "skyblock.sea_monsters"),
            Map.entry(ItemType.GREATER_BACKPACK, "skyblock.storage_forever"),
            Map.entry(ItemType.FEATHER_ARTIFACT, "skyblock.king_of_the_chickens"),
            Map.entry(ItemType.ARTIFACT_POTION_AFFINITY, "skyblock.true_alchemist"),
            Map.entry(ItemType.TITANIC_EXP_BOTTLE, "skyblock.the_one_bottle"),
            Map.entry(ItemType.NIGHT_VISION_CHARM, "skyblock.night_eyes"),
            Map.entry(ItemType.MAGICAL_WATER_BUCKET, "skyblock.ocean_in_a_bucket"),
            Map.entry(ItemType.PET_ITEM_SADDLE, "skyblock.saddle_up")
    );

    private static final Map<ItemType, String> MINION_UPGRADES = Map.of(
            ItemType.MINION_EXPANDER, "skyblock.more_space",
            ItemType.ENCHANTED_LAVA_BUCKET, "skyblock.super_fuel",
            ItemType.SUPER_COMPACTOR_3000, "skyblock.time_to_go_on_vacation"
    );

    private static final int SYPHON_GOAL = 50;
    private static final int MASS_PRODUCTION_TIER = 11;

    private SkyBlockItemAchievements() {
    }

    public static void onCrafted(SkyBlockPlayer player, ItemType type, boolean minion, int minionTier,
                                 boolean newMinionSlot) {
        PlayerAchievementHandler handler = player.getAchievementHandler();
        String id = type == null ? null : CRAFTED.get(type);
        if (id != null) handler.completeAchievement(id);
        if (!minion) return;

        DatapointMinionData.ProfileMinionData data = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.MINION_DATA, DatapointMinionData.class).getValue();
        handler.setProgress("skyblock.minion_lover", data.getUniqueMinions());
        if (minionTier >= MASS_PRODUCTION_TIER) handler.completeAchievement("skyblock.mass_production");
        if (newMinionSlot) handler.completeAchievement("skyblock.production_expanded");
    }

    public static void onMinionUpgrade(SkyBlockPlayer player, ItemType type) {
        String id = type == null ? null : MINION_UPGRADES.get(type);
        if (id != null) player.getAchievementHandler().completeAchievement(id);
    }

    public static void onSyphon(SkyBlockPlayer player) {
        if (player.getHuntingData().uniqueAttributes() >= SYPHON_GOAL) {
            player.getAchievementHandler().completeAchievement("skyblock.weird_science");
        }
    }

    public static void onObtained(SkyBlockPlayer player, ItemType type) {
        if (type == null) return;
        PlayerAchievementHandler handler = player.getAchievementHandler();
        switch (type) {
            case MIDAS_SWORD, MIDAS_STAFF, STARRED_MIDAS_SWORD, STARRED_MIDAS_STAFF ->
                    handler.completeAchievement("skyblock.united_in_blood");
            case SEAL_OF_THE_FAMILY -> handler.completeAchievement("skyblock.lifelong_contract");
            default -> {
                if (type.name().startsWith("ABIPHONE_")) handler.completeAchievement("skyblock.ring_ring_who_s_this");
            }
        }
    }

    public static void onGreaterAccessoryBagCheck(SkyBlockPlayer player) {
        if (player.hasCustomCollectionAward(CustomCollectionAward.ACCESSORY_BAG_UPGRADE_3)) {
            player.getAchievementHandler().completeAchievement("skyblock.accessories_galore");
        }
    }

    public static void onMinionSold(IslandMinionData.IslandMinion minion, ItemType shippingItem) {
        if (shippingItem != ItemType.ENCHANTED_HOPPER) return;
        for (SkyBlockIsland island : SkyBlockIsland.loaded()) {
            IslandMinionData minions = island.getMinionData();
            if (minions == null || !minions.getMinions().contains(minion)) continue;
            IslandMembers.onlineMembers(island.getIslandID(), island.getCoop())
                    .forEach(member -> member.getAchievementHandler().completeAchievement("skyblock.advanced_transportation"));
            return;
        }
    }
}
