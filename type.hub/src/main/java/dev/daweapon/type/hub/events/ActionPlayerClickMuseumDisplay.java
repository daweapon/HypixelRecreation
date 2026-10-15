package dev.daweapon.type.hub.events;

import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.gui.inventories.museum.GUIMuseumEmptyDisplay;
import dev.daweapon.type.skyblockgeneric.gui.inventories.museum.GUIMuseumNonEmptyDisplay;
import dev.daweapon.type.skyblockgeneric.museum.MuseumDisplayEntityImpl;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Opens the museum item view GUI when a player clicks a museum display case in the Hub museum.
public class ActionPlayerClickMuseumDisplay implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (event.getTarget() instanceof MuseumDisplayEntityImpl museumDisplayEntity) {
            boolean isEmpty = museumDisplayEntity.isEmpty();

            if (isEmpty) {
                new GUIMuseumEmptyDisplay(
                        museumDisplayEntity.getDisplay(),
                        museumDisplayEntity.getPositionInMuseum()
                ).open(player);
            } else {
                new GUIMuseumNonEmptyDisplay(
                        museumDisplayEntity.getDisplay(),
                        museumDisplayEntity.getPositionInMuseum()
                ).open(player);
            }
        }
    }
}
