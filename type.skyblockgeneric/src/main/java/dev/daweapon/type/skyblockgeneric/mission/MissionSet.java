package dev.daweapon.type.skyblockgeneric.mission;

import lombok.Getter;
import lombok.SneakyThrows;
import dev.daweapon.type.skyblockgeneric.mission.missions.*;
import dev.daweapon.type.skyblockgeneric.mission.missions.barn.*;
import dev.daweapon.type.skyblockgeneric.mission.missions.garden.*;
import dev.daweapon.type.skyblockgeneric.mission.missions.barn.MissionTalkToFarmHand;
import dev.daweapon.type.skyblockgeneric.mission.missions.barn.MissionTalkToFarmhandAgain;
import dev.daweapon.type.skyblockgeneric.mission.missions.blacksmith.MissionMineCoal;
import dev.daweapon.type.skyblockgeneric.mission.missions.blacksmith.MissionTalkToBlacksmith;
import dev.daweapon.type.skyblockgeneric.mission.missions.blacksmith.MissionTalkToBlacksmithAgain;
import dev.daweapon.type.skyblockgeneric.mission.missions.farmer.MissionCollectWheat;
import dev.daweapon.type.skyblockgeneric.mission.missions.farmer.MissionTalkToFarmer;
import dev.daweapon.type.skyblockgeneric.mission.missions.farmer.MissionTalkToFarmerAgain;
import dev.daweapon.type.skyblockgeneric.mission.missions.goldmine.lazyminer.MissionFindLazyMinerPickaxe;
import dev.daweapon.type.skyblockgeneric.mission.missions.goldmine.lazyminer.MissionTalkToLazyMiner;
import dev.daweapon.type.skyblockgeneric.mission.missions.lumber.MissionBreakOaklog;
import dev.daweapon.type.skyblockgeneric.mission.missions.lumber.MissionTalkToLumberjack;
import dev.daweapon.type.skyblockgeneric.mission.missions.lumber.MissionTalkToLumberjackAgain;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.birchpark.*;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.darkthicket.*;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.jungle.*;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.savanna.MissionCheckOnMelody;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.savanna.MissionCollectAcaciaLogs;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.savanna.MissionGiveMelodyAcaciaLogs;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.savanna.MissionTravelToTheSavannaWoodland;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

@Getter
public enum MissionSet {
    GETTING_STARTED(MissionBreakLog.class, MissionCraftWorkbench.class, MissionCraftWoodenPickaxe.class, MissionTalkJerry.class, MissionUseTeleporter.class),
    SAVING_UP(MissionTalkToBanker.class, MissionDepositCoinsInBank.class),
    LIBRARY_CARD(MissionTalkToLibrarian.class),
    AUCTIONEER(MissionTalkToAuctionMaster.class),
    TIME_TO_MINE(MissionTalkToBlacksmith.class, MissionMineCoal.class, MissionTalkToBlacksmithAgain.class),
    TIME_TO_STRIKE(MissionKillZombies.class, MissionTalkToBartender.class),
    TIMBER(MissionTalkToLumberjack.class, MissionBreakOaklog.class, MissionTalkToLumberjackAgain.class),
    INTO_THE_WOODS(MissionTravelToThePark.class, MissionTalkToCharlie.class, MissionCollectBirchLogs.class, MissionGiveCharlieBirchLogs.class, MissionClaimTheTrousers.class, MissionTalkToCharlieAgain.class),
    FIRST_HARVEST(MissionTalkToFarmer.class, MissionCollectWheat.class, MissionTalkToFarmerAgain.class),
    BACK_AT_THE_BARNYARD(MissionTalkToFarmHand.class, MissionCraftWheatMinion.class, MissionTalkToFarmhandAgain.class, MissionFarmhandAddPet.class, MissionFarmhandSummonPet.class,
            MissionFarmhandCollectPotato.class, MissionFarmhandCollectCarrot.class, MissionFarmhandCollectMelon.class, MissionFarmhandCollectPumpkin.class,
            MissionFarmhandCollectLeather.class, MissionFarmhandCollectChicken.class, MissionFarmhandCollectPork.class, MissionReachFarmingFive.class),
    CARPENTRY(MissionGiveWoolToCarpenter.class),
    LOST_AND_FOUND(MissionFindLazyMinerPickaxe.class, MissionTalkToLazyMiner.class),
    THE_CAMPFIRE_CULT(MissionTravelToTheDarkThicket.class, MissionFindTheCampfire.class, MissionSneakUpOnRyan.class, MissionCompleteTrialOfFireOne.class, MissionTalkToRyan.class, MissionCollectDarkOakLogs.class, MissionGiveRyanDarkOakLogs.class),
    THE_REBUILD(MissionTravelToTheSavannaWoodland.class, MissionCheckOnMelody.class, MissionCollectAcaciaLogs.class, MissionGiveMelodyAcaciaLogs.class),
    HELPFUL_FARMER(MissionTalkToSam.class, MissionBringWheatToSam.class),
    LEARNING_TO_COMPOST(MissionTalkToSamAtComposter.class, MissionPurchaseFuel.class, MissionRefuelComposter.class, MissionTakeCompost.class),
    PREPPING_THE_PLOT(MissionPurchasePlot.class, MissionCleanPlot.class, MissionTalkToSamPlotCleaned.class),
    CROP_N_PROFIT(MissionBringBreadWheat.class, MissionSetUpStand.class, MissionServeFirstCustomer.class),
    ROOTING_FOR_YOU(MissionGiveSamBlueprint.class, MissionCollectResourcesForSam.class, MissionPasteGreenhouse.class, MissionWaitForGreenhouse.class),
    MOLE_PROBLEMS(MissionTalkToMolbert.class, MissionCollectJungleLogs.class, MissionGiveMolbertJungleLogs.class, MissionLeaveTheArea.class, MissionTalkToMolbertAgain.class, MissionPlaceTraps.class, MissionTalkToMolbertAgainAgain.class,
            MissionLeaveTheAreaAgain.class, MissionTalkToMolbertAgainAgainAgain.class)
    ;

    private final Class<? extends SkyBlockMission>[] missions;

    @SafeVarargs
    MissionSet(Class<? extends SkyBlockMission>... missions) {
        this.missions = missions;
    }

    /**
     * @param missionID The mission ID to check
     * @return The mission set that the mission is in, or null if it is not in a set
     */
    @SneakyThrows
    public static MissionSet getFromMission(String missionID) {
        for (MissionSet missionSet : MissionSet.values()) {
            for (Class<? extends SkyBlockMission> mission : missionSet.missions) {
                if (mission.newInstance().getID().equalsIgnoreCase(missionID)) {
                    return missionSet;
                }
            }
        }

        return null;
    }

    /**
     * @param player The player to check
     * @return Whether the player has completed all missions in the set
     */
    @SneakyThrows
    public boolean hasCompleted(SkyBlockPlayer player) {
        for (Class<? extends SkyBlockMission> mission : missions) {
            if (!player.getMissionData().hasCompleted(mission.newInstance().getID())) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param player The player to get the next mission for
     * @return The next mission in the set, or null if there are no more missions
     */
    @SneakyThrows
    public @Nullable Class<? extends SkyBlockMission> getNextMission(SkyBlockPlayer player) {
        for (Class<? extends SkyBlockMission> mission : missions) {
            if (!player.getMissionData().hasCompleted(mission.newInstance().getID())) {
                return mission;
            }
        }

        return null;
    }
}
