package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Updates the action bar with the current region's name when a player changes region
public class ActionRegionChangeDisplay implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerRegionChangeEvent event) {
        SkyBlockPlayer player = event.getPlayer();

        if (event.getTo() != null && event.getFrom() != null
                && !event.getTo().equals(event.getFrom())) {

            RegionType type = event.getTo();
            String name = type.getName();
            if (type == RegionType.PLAYER_MUSEUM) {
                name = name.formatted(player.getUsername());
            }

            SkyBlockActionBar.getFor(player).addReplacement(
                    SkyBlockActionBar.BarSection.DEFENSE,
                    Text.of("<color:{}> ⏣ {}", type.getColor(), name),
                    20,
                    2
            );
        }
    }
}
