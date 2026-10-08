package net.swofty.type.crimsonisle.faction;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.crimson.questline.BarbarianStep;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonAbiphone;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Twice a second: the Throne Room and pre-faction Chief's Hut gates, walking into the Chief's Hut
 * with Ugo, picking up stolen research, and the Dean's contact once a Mage owns an Abiphone.
 */
public final class FactionQuestTicker {
    private static final double RESEARCH_RADIUS = 2.5;

    private FactionQuestTicker() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(FactionQuestTicker::tick)
                .repeat(TaskSchedule.tick(10)).schedule();
        FactionVillagers.startParticles();
    }

    private static void tick() {
        for (var online : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (online instanceof SkyBlockPlayer player && player.isOnline() && player.getSkyblockDataHandler() != null) {
                check(player);
            }
        }
    }

    private static void check(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        RegionType type = region == null ? null : region.getType();
        if (type == RegionType.THRONE_ROOM && !ThroneRoomGate.mayEnter(player)) ThroneRoomGate.turnAway(player);
        if (type == RegionType.CHIEFS_HUT) {
            if (!ThroneRoomGate.mayEnterChiefsHut(player)) ThroneRoomGate.turnAwayFromChiefsHut(player);
            else if (CrimsonQuestlines.barbarian(player) == BarbarianStep.ESCORT_UGO) BarbarianScenes.chiefWelcome(player);
        }
        if (CrimsonScene.isPlaying(player)) return;
        MageStep mage = CrimsonQuestlines.mage(player);
        if (mage == MageStep.RECOVER_RESEARCH && player.getPosition().distance(MageScenes.BASTION_RESEARCH) <= RESEARCH_RADIUS) {
            MageScenes.researchRecovered(player);
        }
        if (mage == MageStep.BUY_ABIPHONE && CrimsonAbiphone.hasAbiphone(player) && !CrimsonAbiphone.hasContact(player, "dean")) {
            CrimsonAbiphone.grant(player, "dean");
        }
        if (CrimsonQuestlines.barbarian(player) == BarbarianStep.STEAL_RESEARCH
                && player.getPosition().distance(BarbarianScenes.DEAN_OFFICE_RESEARCH) <= RESEARCH_RADIUS) {
            BarbarianScenes.researchStolen(player);
        }
    }
}
