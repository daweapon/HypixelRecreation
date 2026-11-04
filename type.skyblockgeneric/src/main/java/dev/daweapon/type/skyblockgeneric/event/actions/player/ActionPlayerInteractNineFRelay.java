package dev.daweapon.type.skyblockgeneric.event.actions.player;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.abiphone.ninef.EntityNineFRelay;
import dev.daweapon.type.skyblockgeneric.abiphone.ninef.GUIRelayMinigame;
import dev.daweapon.type.skyblockgeneric.abiphone.ninef.NineFNetwork;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;

// Opens the 9f Network relay minigame when a player who can see a relay clicks it
public class ActionPlayerInteractNineFRelay implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN || !(event.getTarget() instanceof EntityNineFRelay relay)) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!NineFNetwork.canSee(player, relay.relay())) return;
        player.openView(new GUIRelayMinigame(), GUIRelayMinigame.start(relay.relay()));
    }
}
