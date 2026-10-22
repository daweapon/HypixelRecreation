package dev.daweapon.type.thefarmingislands.events;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.thefarmingislands.treasurehunter.TreasureHunter;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;

public class ActionTreasureHunterDig implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onDig(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!player.getItemInMainHand().material().name().endsWith("_shovel")) return;
        if (!TreasureHunter.isDigSpot(player, event.getBlockPosition())) return;
        TreasureHunter.dig(player);
    }
}
