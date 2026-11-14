package dev.daweapon.type.ravengardgeneric.event.actions.custom;

import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.ravengardgeneric.event.custom.PlayerRegionChangeEvent;
import dev.daweapon.type.ravengardgeneric.profile.RavengardProfiles;
import dev.daweapon.type.ravengardgeneric.region.RavengardRegionType;

import java.time.Duration;

public class ActionRegionDiscovered implements HypixelEventClass {
    private static final Duration FADE = Duration.ofSeconds(1);

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true)
    public void run(PlayerRegionChangeEvent event) {
        RavengardRegionType to = event.getTo();
        if (to == null) {
            return;
        }

        if (RavengardProfiles.hasDiscovered(event.getPlayer(), to)) {
            event.getPlayer().sendActionBar(
                    Text.of("<f>{} discovered", to.getDisplayName()).asComponent());
            return;
        }
        RavengardProfiles.markDiscovered(event.getPlayer(), to);

        // one second in, one second out, no hold in between
        event.getPlayer().sendTitlePart(TitlePart.TIMES, Title.Times.times(FADE, Duration.ZERO, FADE));
        event.getPlayer().sendTitlePart(TitlePart.TITLE, Text.empty());
        event.getPlayer().sendTitlePart(TitlePart.SUBTITLE, "<f>{} discovered", to.getDisplayName());
    }
}
