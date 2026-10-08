package net.swofty.type.crimsonisle.events;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.block.Block;
import net.swofty.type.crimsonisle.quest.rescue.RescueService;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTestCompletedEvent;
import net.swofty.type.skyblockgeneric.crimson.quest.FactionQuestService;
import net.swofty.type.skyblockgeneric.event.custom.CrimsonMinibossKilledEvent;
import net.swofty.type.skyblockgeneric.event.custom.KuudraClearedEvent;
import net.swofty.type.skyblockgeneric.event.custom.TrophyFishCaughtEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Feeds game events into the daily Faction Quests and lets bells distract rescue-mission guards.
public class ActionFactionQuestProgress implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onTrophyFish(TrophyFishCaughtEvent event) {
        FactionQuestService.onTrophyFish(event.getPlayer(), event.getFishId());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onMiniboss(CrimsonMinibossKilledEvent event) {
        FactionQuestService.onMiniboss(event.player(), event.miniboss());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onDojo(DojoTestCompletedEvent event) {
        FactionQuestService.onDojo(event.player(), event.test(), event.grade());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKuudra(KuudraClearedEvent event) {
        FactionQuestService.onKuudraCleared(event.player(), event.tier());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onBell(PlayerBlockInteractEvent event) {
        if (!event.getBlock().compare(Block.BELL)) return;
        RescueService.ringBell((SkyBlockPlayer) event.getPlayer(), event.getBlockPosition());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onDisconnect(PlayerDisconnectEvent event) {
        RescueService.forget((SkyBlockPlayer) event.getPlayer());
    }
}
