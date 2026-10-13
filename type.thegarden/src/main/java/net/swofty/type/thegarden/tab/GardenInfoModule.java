package net.swofty.type.thegarden.tab;

import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.tab.TablistSkinRegistry;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenProgress;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GardenInfoModule extends TablistModule {

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        Locale l = player.getLocale();
        GardenProfile garden = Gardens.profile(player);
        int level = GardenProgress.level(garden);

        List<TablistEntry> entries = new ArrayList<>(List.of(
                centered(Text.key("tablist.module.server_info"), l, TablistSkinRegistry.CYAN)
        ));
        entries.add(new TablistEntry(Text.key("tablist.server_info.area.garden"), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.server_info.server_label", HypixelConst.getServerName()), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.garden.gems", StringUtility.commaify(player.getGems())), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.garden.level",
                level > 0 ? StringUtility.getAsRomanNumeral(level) : "0"), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.garden.copper", StringUtility.commaify(Math.floor(garden.getCopper()))), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.garden.sowdust", StringUtility.commaify(Math.floor(garden.getSowdust()))), TablistSkinRegistry.GRAY));
        VisitorData visitors = garden.visitors();
        entries.add(getGrayEntry());
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
