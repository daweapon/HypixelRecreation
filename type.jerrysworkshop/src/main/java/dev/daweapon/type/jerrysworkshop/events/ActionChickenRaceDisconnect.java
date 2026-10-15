package dev.daweapon.type.jerrysworkshop.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.jerrysworkshop.chickenrace.ChickenRace;
import dev.daweapon.type.jerrysworkshop.present.GiftCompass;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionChickenRaceDisconnect implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerDisconnectEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        ChickenRace.quit(player);
        GiftCompass.forget(player);
    }
}
