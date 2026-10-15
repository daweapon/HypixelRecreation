package dev.daweapon.type.crimsonisle.events;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.crimsonisle.quest.rescue.RescueService;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTestCompletedEvent;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestService;
import dev.daweapon.type.skyblockgeneric.event.custom.CrimsonMinibossKilledEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.TrophyFishCaughtEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
