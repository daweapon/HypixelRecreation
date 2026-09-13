package net.swofty.type.skyblockgeneric.staff;

import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.bank.BankAccountTier;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryData;
import net.swofty.type.skyblockgeneric.collection.CollectionCategory;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBackpacks;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCollectedMobTypeRewards;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCollection;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointFairySouls;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMinionData;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMissionData;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointStorage;
import net.swofty.type.skyblockgeneric.data.fairysouls.FairySoulCatalog;
import net.swofty.type.skyblockgeneric.elections.FameRank;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategories;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.minion.MinionRegistry;
import net.swofty.type.skyblockgeneric.minion.SkyBlockMinion;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.rabbits.ChocolateRabbit;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.skill.SkillCategory;
import net.swofty.type.skyblockgeneric.slayer.SlayerDefinition;
import net.swofty.type.skyblockgeneric.slayer.SlayerLevelReward;
import net.swofty.type.skyblockgeneric.slayer.SlayerRegistry;
import net.swofty.type.skyblockgeneric.slayer.SlayerType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoul;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoulExchangeLevels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MaxStickService {
    private static final double MAX_PURSE_COINS = 1_000_000_000_000D;
    // Matches GUIStorage's rendering bounds: 18 backpack slots (27-44) and 9 ender chest pages (9-17).
    private static final int MAX_BACKPACK_SLOTS = 18;
    private static final int MAX_ENDER_CHEST_PAGES = 9;

    private static final long MAX_CHOCOLATE = 1_000_000_000_000L;
    private static final int MAX_RABBIT_BARN_LEVEL = 247;
    private static final int MAX_HAND_BAKED_CHOCOLATE_LEVEL = 10;
    private static final int MAX_TIME_TOWER_LEVEL = 15;
    private static final int MAX_TIME_TOWER_CHARGES = 3;
    private static final int MAX_RABBIT_SHRINE_LEVEL = 20;
    private static final int MAX_COACH_JACKRABBIT_LEVEL = 20;
    private static final int MAX_EMPLOYEE_LEVEL = 225;

    private static final double MOB_TYPE_COMBAT_XP_REWARD = 1000D;
    private static final int MOB_TYPE_COIN_REWARD = 1000;

    private static final int MAX_BITS = 100_000_000;

    private MaxStickService() {}

    public static void initialize() {
        InteractableItemConfig config = InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> giveMaxProfile(player))
                .leftClickHandler((player, item) -> giveMaxProfile(player))
                .inventoryInteractHandler((player, item) -> {
                    giveMaxProfile(player);
                    return false;
                })
                .build();

        InteractableRegistry.register("THE_MAX_STICK_INTERACT", config);
    }

    public static void giveMaxProfile(SkyBlockPlayer player) {
        if (!player.getRank().isEqualOrHigherThan(Rank.STAFF)) {
            player.sendMessage("<c>You do not have permission to use this item.");
            return;
        }

        maxProfile(player);
    }

    public static void maxProfile(SkyBlockPlayer player) {
        maxSkills(player);
        maxCollections(player);
        maxCoins(player);
        maxBank(player);
        maxSlayers(player);
        maxHotm(player);
        maxFairySouls(player);
        maxBestiary(player);
        maxBackpackSlots(player);
        maxEnderChestPages(player);
        maxMinionCrafts(player);
        maxMissions(player);
        maxChocolateFactory(player);
        maxMobTypes(player);
        maxFame(player);
        giveMaxBits(player);

        player.sendMessage("<a><l>MAXED! <f>Your profile has been fully maxed out.");
    }

    private static void maxSkills(SkyBlockPlayer player) {
        DatapointSkills.PlayerSkills skills = player.getSkills();

        for (SkillCategories category : SkillCategories.values()) {
            SkillCategory skillCategory = category.asCategory();
            double totalXp = 0;
            for (int level = 1; level <= skillCategory.getHighestLevel(); level++) {
                totalXp += skillCategory.getReward(level).requirement();
            }
            skills.setRaw(player, category, totalXp);
        }
    }

    private static void maxCollections(SkyBlockPlayer player) {
        DatapointCollection.PlayerCollection collection = player.getCollection();

        for (CollectionCategory category : CollectionCategories.getCategories()) {
            for (CollectionCategory.ItemCollection itemCollection : category.getCollections()) {
                CollectionCategory.ItemCollectionReward[] rewards = itemCollection.rewards();
                if (rewards.length == 0) continue;

                int maxRequirement = rewards[rewards.length - 1].requirement();
                collection.set(itemCollection.type(), maxRequirement);
            }
        }
    }

    private static void maxCoins(SkyBlockPlayer player) {
        player.setCoins(MAX_PURSE_COINS);
    }

    private static void maxBank(SkyBlockPlayer player) {
        DatapointBankData datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class);

        synchronized (datapoint) {
            DatapointBankData.BankData bankData = datapoint.getValue();
            bankData.setAccountTier(BankAccountTier.PALATIAL);
            bankData.setAmount(BankAccountTier.PALATIAL.getCapacity());
            datapoint.setValue(bankData);
        }
    }

    private static void maxSlayers(SkyBlockPlayer player) {
        DatapointSlayer.SlayerData slayerData = player.getSlayerData();

        for (SlayerType type : SlayerType.values()) {
            SlayerDefinition definition = SlayerRegistry.get(type);
            if (definition == null || definition.levels().isEmpty()) continue;

            int maxXp = definition.levels().stream().mapToInt(SlayerLevelReward::requiredXp).max().orElse(0);
            slayerData.progress(type).setXp(maxXp);
        }
    }

    private static void maxHotm(SkyBlockPlayer player) {
        DatapointHOTM datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class);

        synchronized (datapoint) {
            DatapointHOTM.PlayerHOTMData hotm = datapoint.getValue();
            hotm.setExperience(1_247_000L);
            hotm.setMithrilPowder(1_000_000L);
            hotm.setGemstonePowder(1_000_000L);
            hotm.setTokens(100);
            datapoint.setValue(hotm);
        }
    }

    private static void maxFairySouls(SkyBlockPlayer player) {
        DatapointFairySouls.PlayerFairySouls fairySouls = player.getFairySouls();

        for (FairySoul soul : FairySoulCatalog.getAllSouls()) {
            if (!fairySouls.getAllFairySouls().contains(soul.getId())) {
                fairySouls.addCollectedFairySouls(soul.getId());
            }
        }

        // Auto-claim: exchange every group of 5 collected souls, same as talking to Tia,
        // so the SkyBlock XP tied to each exchange level is actually granted (backpack
        // slots from this loop get overwritten by the flat max in maxBackpackSlots below).
        FairySoulExchangeLevels nextLevel;
        while ((nextLevel = fairySouls.getNextExchangeLevel()) != null
                && fairySouls.getCollectedFairySouls().size() >= 5) {
            fairySouls.exchange();
            player.getSkyBlockExperience().addExperience(SkyBlockLevelCause.getFairySoulExchangeCause(nextLevel.ordinal()));
        }

        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FAIRY_SOULS, DatapointFairySouls.class).setValue(fairySouls);
    }

    private static void maxBestiary(SkyBlockPlayer player) {
        DatapointBestiary.PlayerBestiary bestiary = player.getBestiaryData();
        BestiaryData bestiaryData = new BestiaryData();

        for (BestiaryCategories category : BestiaryCategories.values()) {
            for (BestiaryMob mob : category.getAllMobs()) {
                int maxKills = bestiaryData.getTotalKillsForMaxTier(mob);
                bestiary.set(mob, maxKills);
            }
        }

        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BRAMASS_BEASTSLAYER, true);
    }

    private static void maxMobTypes(SkyBlockPlayer player) {
        DatapointCollectedMobTypeRewards.PlayerCollectedMobTypeRewards rewards = player.getCollectedMobTypesData();

        for (MobType mobType : MobType.values()) {
            if (rewards.hasClaimed(mobType.name())) continue;

            player.getSkills().increase(player, SkillCategories.COMBAT, MOB_TYPE_COMBAT_XP_REWARD);
            player.addCoins(MOB_TYPE_COIN_REWARD);
            rewards.claim(mobType.name());
        }
    }

    private static void maxFame(SkyBlockPlayer player) {
        long maxFameRequired = FameRank.PARAGON.getFameRequired();
        player.setFame((int) maxFameRequired);
    }

    private static void giveMaxBits(SkyBlockPlayer player) {
        player.setBits(MAX_BITS);
    }

    private static void maxBackpackSlots(SkyBlockPlayer player) {
        DatapointBackpacks datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class);

        synchronized (datapoint) {
            DatapointBackpacks.PlayerBackpacks backpacks = datapoint.getValue();
            backpacks.setUnlockedSlots(MAX_BACKPACK_SLOTS);
            datapoint.setValue(backpacks);
        }
    }

    private static void maxEnderChestPages(SkyBlockPlayer player) {
        DatapointStorage datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.STORAGE, DatapointStorage.class);

        synchronized (datapoint) {
            DatapointStorage.PlayerStorage storage = datapoint.getValue();
            for (int page = 1; page <= MAX_ENDER_CHEST_PAGES; page++) {
                storage.addPage(page);
            }
            datapoint.setValue(storage);
        }
    }

    private static void maxMinionCrafts(SkyBlockPlayer player) {
        DatapointMinionData datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.MINION_DATA, DatapointMinionData.class);

        synchronized (datapoint) {
            List<Map.Entry<String, List<Integer>>> craftedMinions = new ArrayList<>();

            for (MinionRegistry minion : MinionRegistry.values()) {
                List<Integer> tiers = minion.asSkyBlockMinion().getTiers().stream()
                        .map(SkyBlockMinion.MinionTier::tier)
                        .toList();
                craftedMinions.add(Map.entry(minion.name(), tiers));
            }

            datapoint.setValue(new DatapointMinionData.ProfileMinionData(craftedMinions));
        }
    }

    private static void maxMissions(SkyBlockPlayer player) {
        DatapointMissionData datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.MISSION_DATA, DatapointMissionData.class);

        synchronized (datapoint) {
            MissionData missionData = datapoint.getValue();

            for (String missionID : MissionData.getAllMissionIDs()) {
                if (missionData.hasCompleted(missionID)) continue;

                missionData.getActiveMissions().removeIf(mission -> mission.getMissionID().equals(missionID));
                missionData.getCompletedMissions().add(new MissionData.ActiveMission(missionID, 0, false));
            }

            datapoint.setValue(missionData);
        }
    }

    private static void maxChocolateFactory(SkyBlockPlayer player) {
        DatapointChocolateFactory datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class);

        synchronized (datapoint) {
            DatapointChocolateFactory.ChocolateFactoryData data = datapoint.getValue();
            data.setChocolate(MAX_CHOCOLATE);
            data.setChocolateAllTime(MAX_CHOCOLATE);
            data.setRabbitBarnLevel(MAX_RABBIT_BARN_LEVEL);
            data.setHandBakedChocolateLevel(MAX_HAND_BAKED_CHOCOLATE_LEVEL);
            data.setTimeTowerLevel(MAX_TIME_TOWER_LEVEL);
            data.setTimeTowerCharges(MAX_TIME_TOWER_CHARGES);
            data.setRabbitShrineLevel(MAX_RABBIT_SHRINE_LEVEL);
            data.setCoachJackrabbitLevel(MAX_COACH_JACKRABBIT_LEVEL);

            for (DatapointChocolateFactory.EmployeeType employeeType : DatapointChocolateFactory.EmployeeType.values()) {
                data.setEmployee(employeeType, MAX_EMPLOYEE_LEVEL);
            }

            for (ChocolateRabbit rabbit : ChocolateRabbit.values()) {
                data.addFoundRabbit(rabbit);
            }

            datapoint.setValue(data);
        }
    }
}
