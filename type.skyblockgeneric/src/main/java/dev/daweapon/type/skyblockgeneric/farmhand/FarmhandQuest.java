package dev.daweapon.type.skyblockgeneric.farmhand;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.mission.missions.barn.*;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;

import java.util.List;

public final class FarmhandQuest {
    public static final String CHICKEN_PET_GIVEN = "npc.farmhand.chicken_pet";

    private static final List<Class<? extends FarmhandCollectionMission>> COLLECTIONS = List.of(
            MissionFarmhandCollectPotato.class,
            MissionFarmhandCollectCarrot.class,
            MissionFarmhandCollectMelon.class,
            MissionFarmhandCollectPumpkin.class,
            MissionFarmhandCollectLeather.class,
            MissionFarmhandCollectChicken.class,
            MissionFarmhandCollectPork.class
    );

    private FarmhandQuest() {
    }

    public static boolean petGiven(SkyBlockPlayer player) {
        return ChapterTriggers.done(player, CHICKEN_PET_GIVEN);
    }

    public static void giveChickenPet(SkyBlockPlayer player) {
        if (petGiven(player)) return;
        ChapterTriggers.fire(player, CHICKEN_PET_GIVEN);
        SkyBlockItem pet = new SkyBlockItem(ItemType.CHICKEN_PET);
        pet.getAttributeHandler().setRarity(Rarity.COMMON);
        player.addAndUpdateItem(pet);
    }

    public static void startPetLessons(SkyBlockPlayer player) {
        MissionData data = player.getMissionData();
        if (hasStartedOrCompleted(data, MissionFarmhandAddPet.class)) return;
        data.setSkyBlockPlayer(player);
        data.startMission(MissionFarmhandAddPet.class);
    }

    public static void startCollections(SkyBlockPlayer player) {
        MissionData data = player.getMissionData();
        Class<? extends FarmhandCollectionMission> first = COLLECTIONS.getFirst();
        if (hasStartedOrCompleted(data, first)) return;
        data.setSkyBlockPlayer(player);
        data.startMission(first);
    }

    public static void startFarmingFive(SkyBlockPlayer player) {
        MissionData data = player.getMissionData();
        if (hasStartedOrCompleted(data, MissionReachFarmingFive.class)) return;
        data.setSkyBlockPlayer(player);
        data.startMission(MissionReachFarmingFive.class);
    }

    public static void syncLater(SkyBlockPlayer player) {
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> sync(player));
    }

    public static void sync(SkyBlockPlayer player) {
        MissionData data = player.getMissionData();
        data.setSkyBlockPlayer(player);
        for (Class<? extends FarmhandCollectionMission> mission : COLLECTIONS) {
            if (data.hasCompleted(mission)) continue;
            if (!data.isCurrentlyActive(mission)) break;
            FarmhandCollectionMission instance = (FarmhandCollectionMission) MissionData.getMissionClass(idOf(mission));
            if (player.getCollection().get(instance.collected()) <= 0) break;
            data.endMission(mission);
        }
        if (data.isCurrentlyActive(MissionReachFarmingFive.class)
                && MissionReachFarmingFive.reached(player)) {
            data.endMission(MissionReachFarmingFive.class);
        }
    }

    private static String idOf(Class<? extends SkyBlockMission> mission) {
        try {
            return mission.getDeclaredConstructor().newInstance().getID();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static boolean hasStartedOrCompleted(MissionData data, Class<? extends SkyBlockMission> mission) {
        return data.isCurrentlyActive(mission) || data.hasCompleted(mission);
    }
}
