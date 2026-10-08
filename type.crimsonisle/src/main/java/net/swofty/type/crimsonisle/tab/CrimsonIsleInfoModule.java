package net.swofty.type.crimsonisle.tab;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.tab.TablistSkinRegistry;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crimson.CrimsonTabWidgets;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Server info followed by the Crimson Isle widgets: Reputation, Faction Quests, Trophy Fish and Volcano. */
public class CrimsonIsleInfoModule extends TablistModule {
    private static final int COLUMN_HEIGHT = 20;

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer viewer) {
        SkyBlockPlayer player = (SkyBlockPlayer) viewer;
        Locale locale = player.getLocale();
        List<TablistEntry> entries = new ArrayList<>(List.of(
                centered(Text.key("tablist.module.server_info"), locale, TablistSkinRegistry.CYAN),
                new TablistEntry(Text.key("tablist.server_info.area.crimson_isle"), TablistSkinRegistry.GRAY),
                new TablistEntry(Text.key("tablist.server_info.server_label", HypixelConst.getServerName()), TablistSkinRegistry.GRAY)
        ));

        for (CrimsonTabWidgets.Widget widget : CrimsonTabWidgets.Widget.values()) {
            List<String> lines = CrimsonTabWidgets.lines(widget, player);
            if (entries.size() + 1 + lines.size() > COLUMN_HEIGHT) break;
            entries.add(getGrayEntry());
            lines.forEach(line -> entries.add(new TablistEntry(line, TablistSkinRegistry.GRAY)));
        }
        return fillRestWithGray(new ArrayList<>(entries));
    }
}
