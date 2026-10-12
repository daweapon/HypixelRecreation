package net.swofty.type.skyblockgeneric.staff;

import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.fishing.trophy.TrophyFishMax;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoProgress;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerProgress;
import net.swofty.type.skyblockgeneric.crimson.town.CrimsonTownProgress;
import net.swofty.type.skyblockgeneric.gifts.BottleOfJyrre;
import net.swofty.type.skyblockgeneric.mythological.gear.MythologicalItemHandlers;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.bank.BankAccountTier;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryData;
import net.swofty.type.skyblockgeneric.carnival.CarnivalNpc;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.CarnivalProfile;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.type.skyblockgeneric.chickenrace.ChickenRaceProgress;
import net.swofty.type.skyblockgeneric.essence.CrimsonEssenceShop;
import net.swofty.type.skyblockgeneric.essence.IceEssenceShop;
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
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointStorage;
import net.swofty.type.generic.data.datapoints.DatapointBoolean;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.skyblockgeneric.bank.PersonalBankService;
import net.swofty.type.skyblockgeneric.data.fairysouls.FairySoulCatalog;
import net.swofty.type.skyblockgeneric.farmingcontest.AnitaShop;
import net.swofty.type.skyblockgeneric.farmingcontest.ContestBracket;
import net.swofty.type.skyblockgeneric.farmingcontest.ContestCrop;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContestProfile;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContests;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags.GUIAccessoryBag;
import net.swofty.type.skyblockgeneric.elections.FameRank;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategories;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.levels.SkyBlockEmblems;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.minion.MinionRegistry;
import net.swofty.type.skyblockgeneric.minion.inferno.ReaperPepper;
import net.swofty.type.skyblockgeneric.minion.SkyBlockMinion;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.museum.MuseumService;
import net.swofty.type.skyblockgeneric.rabbits.ChocolateRabbit;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.skill.SkillCategory;
import net.swofty.type.skyblockgeneric.slayer.SlayerDefinition;
import net.swofty.type.skyblockgeneric.slayer.SlayerProgression;
import net.swofty.type.skyblockgeneric.slayer.SlayerRegistry;
import net.swofty.type.skyblockgeneric.slayer.SlayerRngMeter;
import net.swofty.type.skyblockgeneric.slayer.SlayerType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.warps.TravelScrollIslands;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoul;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoulExchangeLevels;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

    private static final int MAX_CONTEST_MEDALS = 10_000;
    private static final int LUCIUS_FINAL_TIER_DARK_AUCTION_PURCHASES = 7;
    private static final int MAX_CARNIVAL_TOKENS = 100_000;

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

    private record Step(String name, Consumer<SkyBlockPlayer> action) {
    }

    // Last: maxSkyBlockLevel tops the total up to real Hypixel's maximum, so every step above it
    // must have granted its own SkyBlock XP causes first or the top-up would overshoot.
    private static final List<Step> STEPS = List.of(
            new Step("Skills", MaxStickService::maxSkills),
            new Step("Collections", MaxStickService::maxCollections),
            new Step("Coins", MaxStickService::maxCoins),
            new Step("Bank", MaxStickService::maxBank),
            new Step("Slayers", MaxStickService::maxSlayers),
            new Step("Heart of the Mountain", MaxStickService::maxHotm),
            new Step("Fairy Souls", MaxStickService::maxFairySouls),
            new Step("Bestiary", MaxStickService::maxBestiary),
            new Step("Backpack slots", MaxStickService::maxBackpackSlots),
            new Step("Ender Chest pages", MaxStickService::maxEnderChestPages),
            new Step("Minion crafts", MaxStickService::maxMinionCrafts),
            new Step("Missions", MaxStickService::maxMissions),
            new Step("Chocolate Factory", MaxStickService::maxChocolateFactory),
            new Step("Mob types", MaxStickService::maxMobTypes),
            new Step("Fame", MaxStickService::maxFame),
            new Step("Bits", MaxStickService::giveMaxBits),
            new Step("Dark Auction calendar", MaxStickService::unlockDarkAuctionCalendar),
            new Step("Farming contests", MaxStickService::maxFarmingContests),
            new Step("Accessory Bag upgrades", MaxStickService::maxAccessoryBagUpgrades),
            new Step("Personal bank", MaxStickService::maxPersonalBank),
            new Step("Hub upgrades", MaxStickService::maxHubUpgrades),
            new Step("Fast Travel", TravelScrollIslands::unlockAll),
            new Step("Museum", MuseumService::maxOut),
            new Step("Trade Center", MaxStickService::meetTradeCenter),
            new Step("Damia", MaxStickService::meetDamia),
            new Step("Carnival", MaxStickService::maxCarnival),
            new Step("Brain Food", player -> MythologicalItemHandlers.setBrainFoodEaten(player, MythologicalItemHandlers.BRAIN_FOOD_LIMIT)),
            new Step("Refined Bottle of Jyrre", player -> BottleOfJyrre.setRefinedConsumed(player, BottleOfJyrre.REFINED_LIMIT)),
            new Step("Reaper Peppers", player -> ReaperPepper.setEaten(player, ReaperPepper.LIMIT)),
            new Step("Ice Essence shop", IceEssenceShop::maxAvailable),
            new Step("Fossil Essence shop", net.swofty.type.skyblockgeneric.essence.FossilEssenceShop::maxAvailable),
            new Step("Fossil Research Center", net.swofty.type.skyblockgeneric.fossil.FossilService::donateAll),
            new Step("Crimson Essence shop", CrimsonEssenceShop::maxAvailable),
            new Step("Other Essence shops", net.swofty.type.skyblockgeneric.essence.EssenceShops::maxAvailable),
            new Step("Chicken Race", ChickenRaceProgress::maxOut),
            new Step("Crimson reputation", CrimsonReputationService::maxOut),
            new Step("Trophy Fish", TrophyFishMax::maxOut),
            new Step("Dojo", DojoProgress::maxOut),
            new Step("Crimson questlines", CrimsonQuestlines::maxOut),
            new Step("Crimson side quests", CrimsonSideQuests::maxOut),
            new Step("Kuudra follower", KuudraFollowerProgress::maxOut),
            new Step("Kuudra clears", KuudraProgress::maxOut),
            new Step("Crimson town", CrimsonTownProgress::maxOut),
            new Step("Pets", MaxPets::maxOut),
            new Step("Accessory Bag", MaxAccessories::maxOut),
            new Step("Accessory powers", MaxPowers::maxOut),
            new Step("Sack of Sacks", MaxBags::maxSacks),
            new Step("Quiver", MaxBags::maxQuiver),
            new Step("SkyBlock Level", MaxStickService::maxSkyBlockLevel));

    public static void maxProfile(SkyBlockPlayer player) {
        List<String> failed = runSteps(player);
        if (failed.isEmpty()) {
            player.sendMessage("<a><l>MAXED! <f>Your profile has been fully maxed out.");
            return;
        }
        player.sendMessage("<c><l>PARTLY MAXED! <c>These steps failed: <e>" + String.join(", ", failed)
                + "<c>. Everything else was maxed; the errors are in the server log.");
    }

    /** Runs every step even when one throws; returns the names of the steps that failed. */
    public static List<String> runSteps(SkyBlockPlayer player) {
        List<String> failed = new ArrayList<>();
        for (Step step : STEPS) {
            try {
                step.action().accept(player);
            } catch (VirtualMachineError error) {
                throw error;
            } catch (Throwable throwable) {
                Logger.error(throwable, "/maxprofile step '{}' failed for {}", step.name(), player.getUsername());
                failed.add(step.name());
            }
        }
        return failed;
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

            DatapointSlayer.SlayerProgress progress = slayerData.progress(type);
            progress.setXp(Math.max(progress.getXp(), definition.maxXp()));
            definition.tiers().keySet().forEach(tier -> {
                if (progress.completions(tier) <= 0) progress.addCompletion(tier);
            });
            if (SlayerRngMeter.unlocked(progress)) SlayerRngMeter.fill(definition, progress);
        }
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SLAYER, DatapointSlayer.class).setValue(slayerData);
        SlayerProgression.sync(player);
    }

    private static void maxHotm(SkyBlockPlayer player) {
        HotmService.maxOut(player);
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

    // Real Hypixel only shows Dark Auction on the Calendar once a player has physically visited
    // one - testers using /maxprofile or THE_MAX_STICK shouldn't have to wait out a live auction.
    private static void meetTradeCenter(SkyBlockPlayer player) {
        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_LIZ, true);
        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RICHARD, true);
        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_ZARINA, true);
    }

    private static void meetDamia(SkyBlockPlayer player) {
        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_DAMIA, true);
    }

    private static void unlockDarkAuctionCalendar(SkyBlockPlayer player) {
        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_VISITED_DARK_AUCTION, true);
    }

    private static void maxFarmingContests(SkyBlockPlayer player) {
        FarmingContestProfile profile = FarmingContests.profile(player);
        profile.setMetJacob(true);
        profile.setMetAnita(true);
        profile.setPersonalBestsUnlocked(true);
        profile.setExtraFortuneTier(AnitaShop.EXTRA_FORTUNE_TIERS.size());
        profile.setLevelCapTier(AnitaShop.LEVEL_CAP_TIERS.size());
        profile.addMedals(ContestBracket.BRONZE, Math.max(0, MAX_CONTEST_MEDALS - profile.getBronzeMedals()));
        profile.addMedals(ContestBracket.SILVER, Math.max(0, MAX_CONTEST_MEDALS - profile.getSilverMedals()));
        profile.addMedals(ContestBracket.GOLD, Math.max(0, MAX_CONTEST_MEDALS - profile.getGoldMedals()));

        for (ContestCrop crop : ContestCrop.values()) {
            profile.getUniqueBrackets().put(crop, ContestBracket.DIAMOND);
            long cappedPersonalBest = (long) Math.ceil(
                    ContestCrop.PERSONAL_BEST_FORTUNE_CAP * crop.getCropsPerTenthFortune() * 10D);
            profile.getPersonalBests().merge(crop, cappedPersonalBest, Math::max);
        }

        FarmingContests.save(player, profile);
    }

    private static void maxAccessoryBagUpgrades(SkyBlockPlayer player) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.JACOBUS_UPGRADES, DatapointInteger.class)
                .setValue(GUIAccessoryBag.MAX_JACOBUS_UPGRADES);
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.ACCESSORY_BAG_UPGRADES,
                GUIAccessoryBag.MAX_JACOBUS_UPGRADES * GUIAccessoryBag.SKYBLOCK_XP_PER_JACOBUS_UPGRADE);
    }

    private static void maxPersonalBank(SkyBlockPlayer player) {
        DatapointBankData datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class);

        synchronized (datapoint) {
            DatapointBankData.BankData bankData = datapoint.getValue();
            bankData.setPersonalBankLevel(PersonalBankService.COST.length - 1);
            datapoint.setValue(bankData);
        }
    }

    // Lucius's accessories are items, so only the Dark Auction wins that unlock all three tiers are granted.
    private static void maxHubUpgrades(SkyBlockPlayer player) {
        DatapointInteger darkAuctionPurchases = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.DARK_AUCTION_PURCHASES, DatapointInteger.class);
        darkAuctionPurchases.setValue(Math.max(darkAuctionPurchases.getValue(), LUCIUS_FINAL_TIER_DARK_AUCTION_PURCHASES));

        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.RIFT_PRISM_IMBUED, DatapointBoolean.class)
                .setValue(true);
    }

    private static void maxCarnival(SkyBlockPlayer player) {
        CarnivalProfile profile = Carnivals.profile(player);
        for (CarnivalPerk perk : CarnivalPerk.values()) profile.getPerkTiers().put(perk, perk.maxTier());
        for (CarnivalNpc npc : CarnivalNpc.values()) profile.meet(npc);
        profile.setTokens(Math.max(profile.getTokens(), MAX_CARNIVAL_TOKENS));
        Carnivals.save(player, profile);
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

    /**
     * Puts every capped SkyBlock XP task at 100% and unlocks every prefix emblem.
     *
     * <p>The total is {@link SkyBlockXPSource#TOTAL_MAX_XP} (Level 578.74) plus whatever the
     * uncapped tasks (Accessory Bag, Pet Score, ...) hold, since Hypixel counts those on top.
     * Bonus XP absorbs causes such as emblems that belong to no task.
     */
    public static void maxSkyBlockLevel(SkyBlockPlayer player) {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();

        experience.batch(() -> {
            experience.completeAllSourceProgress();

            for (SkyBlockEmblems category : SkyBlockEmblems.values()) {
                for (SkyBlockEmblems.SkyBlockEmblem emblem : category.getEmblems()) {
                    if (emblem.cause() != null) experience.addExperience(emblem.cause());
                }
            }

            experience.setTotalXP(SkyBlockXPSource.TOTAL_MAX_XP + experience.getUncappedSourceXP());
        });

        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SKYBLOCK_EXPERIENCE,
                DatapointSkyBlockExperience.class).setValue(experience);
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
