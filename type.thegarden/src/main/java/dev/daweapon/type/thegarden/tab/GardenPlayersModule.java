package dev.daweapon.type.thegarden.tab;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GardenPlayersModule extends TablistModule {
    private static final int MAX_PLAYERS = 12;

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        List<HypixelPlayer> online = player.getInstance() == null ? List.of(player)
                : player.getInstance().getPlayers().stream().filter(HypixelPlayer.class::isInstance).map(HypixelPlayer.class::cast).toList();
        List<TablistEntry> entries = new ArrayList<>();
        entries.add(centered(Text.of("<b><l>Players <f>({})", online.size()), p.getLocale(), TablistSkinRegistry.CYAN));
        online.stream().limit(MAX_PLAYERS).forEach(other -> entries.add(new TablistEntry(other.getFullDisplayName(), TablistSkinRegistry.GRAY)));
        entries.add(getGrayEntry());
        VisitorData visitors = Gardens.profile(player).visitors();
        entries.add(new TablistEntry(Text.of("<b><l>Visitors: <f>({})", visitors.visible().size()), TablistSkinRegistry.GRAY));
        for (VisitorData.Present present : visitors.visible()) {
            VisitorCatalogue.Visitor visitor = VisitorCatalogue.get().visitor(present.getId());
            entries.add(new TablistEntry(Text.of(" " + (visitor == null ? present.getId() : visitor.coloured())), TablistSkinRegistry.GRAY));
        }
        long wait = Math.max(0, visitors.getNextVisitorAt() - System.currentTimeMillis());
        entries.add(new TablistEntry(Text.of(" <7>Next Visitor: <f>{}", wait == 0 ? "Soon" : (wait / 60_000 + 1) + "m"), TablistSkinRegistry.GRAY));
        fillRestWithGray(entries);
        return entries;
    }
}
