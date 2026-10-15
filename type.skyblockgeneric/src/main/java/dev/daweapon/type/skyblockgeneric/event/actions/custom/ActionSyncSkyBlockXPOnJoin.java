package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContactXP;
import dev.daweapon.type.skyblockgeneric.bank.BankUpgradeAwards;
import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryExperience;
import dev.daweapon.type.skyblockgeneric.entity.mob.CollectedMobTypes;
import dev.daweapon.type.skyblockgeneric.fishing.ship.ShipPartAwards;
import dev.daweapon.type.skyblockgeneric.foraging.TreeGiftMilestones;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeLevelAwards;
import dev.daweapon.type.skyblockgeneric.minion.MinionAwards;
import dev.daweapon.type.skyblockgeneric.mythological.gear.MythologicalItemHandlers;
import dev.daweapon.type.skyblockgeneric.rabbits.HoppitysHuntXP;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerProgression;
import dev.daweapon.type.skyblockgeneric.spookyfestival.SpookyFestivalXP;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.warps.TravelScrollAwards;

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
            HotmService::syncSkyBlockXp,
            dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalNucleus::syncSkyBlockXp);

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        player.getSkyBlockExperience().batch(() -> SYNCS.forEach(sync -> sync.accept(player)));
    }
}
