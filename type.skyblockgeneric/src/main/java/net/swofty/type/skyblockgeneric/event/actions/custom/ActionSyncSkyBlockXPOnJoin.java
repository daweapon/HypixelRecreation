package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.event.player.PlayerSpawnEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneContactXP;
import net.swofty.type.skyblockgeneric.bank.BankUpgradeAwards;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryExperience;
import net.swofty.type.skyblockgeneric.entity.mob.CollectedMobTypes;
import net.swofty.type.skyblockgeneric.fishing.ship.ShipPartAwards;
import net.swofty.type.skyblockgeneric.foraging.TreeGiftMilestones;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.hunting.AttributeLevelAwards;
import net.swofty.type.skyblockgeneric.minion.MinionAwards;
import net.swofty.type.skyblockgeneric.mythological.gear.MythologicalItemHandlers;
import net.swofty.type.skyblockgeneric.rabbits.HoppitysHuntXP;
import net.swofty.type.skyblockgeneric.slayer.SlayerProgression;
import net.swofty.type.skyblockgeneric.spookyfestival.SpookyFestivalXP;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.warps.TravelScrollAwards;

import java.util.List;
import java.util.function.Consumer;

public class ActionSyncSkyBlockXPOnJoin implements HypixelEventClass {
    private static final List<Consumer<SkyBlockPlayer>> SYNCS = List.of(
            BestiaryExperience::syncSkyBlockXP,
            AttributeLevelAwards::syncSkyBlockXP,
            MinionAwards::syncSkyBlockXP,
            SlayerProgression::sync,
            AbiphoneContactXP::syncSkyBlockXP,
            TravelScrollAwards::syncSkyBlockXP,
            HoppitysHuntXP::syncSkyBlockXP,
            SpookyFestivalXP::syncSkyBlockXP,
            BankUpgradeAwards::syncSkyBlockXP,
            TreeGiftMilestones::syncSkyBlockXP,
            ShipPartAwards::syncSkyBlockXP,
            MythologicalItemHandlers::syncSkyBlockXP,
            CollectedMobTypes::syncSkyBlockXP,
            HotmService::syncSkyBlockXp);

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        player.getSkyBlockExperience().batch(() -> SYNCS.forEach(sync -> sync.accept(player)));
    }
}
