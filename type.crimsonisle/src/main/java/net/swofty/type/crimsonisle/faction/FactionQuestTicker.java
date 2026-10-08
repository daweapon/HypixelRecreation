package net.swofty.type.crimsonisle.faction;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.questline.BarbarianStep;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonAbiphone;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.QuestObjectives;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Registers the questline with Crag's Kuudra gate, then twice a second: Elle's intro Blazes, the Throne Room and pre-faction Chief's Hut gates, walking into the Chief's Hut
 * with Ugo, picking up stolen research, and the Dean's contact once a Mage owns an Abiphone.
 */
public final class FactionQuestTicker {
    private static final double RESEARCH_RADIUS = 2.5;

    private FactionQuestTicker() {
    }

    public static void start() {
        KuudraProgress.registerQuestlineCheck(CrimsonQuestlines::isReadyForKuudra);
        QuestObjectives.register(CrimsonQuestObjectives::of);
        MinecraftServer.getSchedulerManager().buildTask(FactionQuestTicker::tick)
                .repeat(TaskSchedule.tick(10)).schedule();
        FactionVillagers.startParticles();
    }

    private static void tick() {
        Set<UUID> online = new HashSet<>();
        for (var each : MinecraftServer.getConnectionManager().getOnlinePlayers()) online.add(each.getUuid());
        IntroBlazes.cleanUp(online);
        for (var each : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (each instanceof SkyBlockPlayer player && player.isOnline() && player.getSkyblockDataHandler() != null) {
                check(player);
            }
        }
    }

    private static void check(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        RegionType type = region == null ? null : region.getType();
        if (type == RegionType.THRONE_ROOM && !ThroneRoomGate.mayEnter(player)) ThroneRoomGate.turnAway(player);
        if (type == RegionType.CHIEFS_HUT) {
            if (!ThroneRoomGate.mayEnterChiefsHut(player)) {
                if (ThroneRoomGate.isInsideChiefsHut(player)) ThroneRoomGate.turnAwayFromChiefsHut(player);
            }
            else if (CrimsonQuestlines.barbarian(player) == BarbarianStep.ESCORT_UGO) BarbarianScenes.chiefWelcome(player);
        }
        IntroBlazes.check(player);
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
