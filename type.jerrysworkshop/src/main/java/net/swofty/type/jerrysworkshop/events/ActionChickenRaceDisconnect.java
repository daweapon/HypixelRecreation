package net.swofty.type.jerrysworkshop.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.jerrysworkshop.chickenrace.ChickenRace;
import net.swofty.type.jerrysworkshop.present.GiftCompass;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionChickenRaceDisconnect implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerDisconnectEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        ChickenRace.quit(player);
        GiftCompass.forget(player);
    }
}
