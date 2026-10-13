package net.swofty.type.crystalhollows.tab;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.tab.TablistSkinRegistry;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CrystalHollowsInfoModule extends TablistModule {

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer viewer) {
        SkyBlockPlayer player = (SkyBlockPlayer) viewer;
        Locale locale = player.getLocale();
        List<TablistEntry> entries = new ArrayList<>(List.of(
                centered(Text.key("tablist.module.server_info"), locale, TablistSkinRegistry.CYAN),
                new TablistEntry(Text.key("tablist.server_info.area.crystal_hollows"), TablistSkinRegistry.GRAY),
                new TablistEntry(Text.key("tablist.server_info.server_label", HypixelConst.getServerName()), TablistSkinRegistry.GRAY),
                getGrayEntry(),
                new TablistEntry("<d><b>Crystals:", TablistSkinRegistry.GRAY)
        ));
        for (NucleusCrystal crystal : NucleusCrystal.values()) {
            entries.add(new TablistEntry(" " + crystal.coloured() + "<7>: " + CrystalNucleus.state(player, crystal).label(),
                    TablistSkinRegistry.GRAY));
        }
        return fillRestWithGray(new ArrayList<>(entries));
    }
}
