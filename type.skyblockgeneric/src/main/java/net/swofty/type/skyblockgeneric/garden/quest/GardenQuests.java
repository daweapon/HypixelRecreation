package net.swofty.type.skyblockgeneric.garden.quest;

import net.swofty.type.skyblockgeneric.garden.npc.NPCSam;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorService;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.mission.missions.garden.*;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class GardenQuests {
    private static final String FIRST_CUSTOMER = "ANDREW";

    private GardenQuests() {
    }

    public static void start(SkyBlockPlayer player, Class<? extends SkyBlockMission> mission) {
        MissionData data = player.getMissionData();
        data.setSkyBlockPlayer(player);
        if (!data.isCurrentlyActive(mission) && !data.hasCompleted(mission)) {
            data.startMission(mission);
        }
    }

    public static boolean end(SkyBlockPlayer player, Class<? extends SkyBlockMission> mission) {
        MissionData data = player.getMissionData();
        data.setSkyBlockPlayer(player);
        if (!data.isCurrentlyActive(mission)) return false;
        data.endMission(mission);
        return true;
    }

    public static void fuelPurchased(SkyBlockPlayer player) {
        end(player, MissionPurchaseFuel.class);
    }

    public static void composterFuelled(SkyBlockPlayer player) {
        end(player, MissionRefuelComposter.class);
    }

    public static void compostCollected(SkyBlockPlayer player) {
        end(player, MissionTakeCompost.class);
    }

    public static void plotUnlocked(SkyBlockPlayer player) {
        if (end(player, MissionPurchasePlot.class)) {
            NPCSam.get().plotUnlocked(player);
        }
    }

    public static void visitorServed(SkyBlockPlayer player) {
        if (end(player, MissionServeFirstCustomer.class)) {
            NPCSam.get().firstCustomerServed(player);
        }
    }

    public static void firstCustomer(SkyBlockPlayer player) {
        VisitorService.callNow(player, FIRST_CUSTOMER);
    }

    public static void plotCleaned(SkyBlockPlayer player) {
        end(player, MissionCleanPlot.class);
    }
}
