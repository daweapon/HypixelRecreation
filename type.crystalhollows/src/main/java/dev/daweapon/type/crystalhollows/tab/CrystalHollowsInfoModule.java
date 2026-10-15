package dev.daweapon.type.crystalhollows.tab;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
                new TablistEntry("<b><l>Crystals:", TablistSkinRegistry.GRAY)
        ));
        for (NucleusCrystal crystal : NucleusCrystal.values()) {
            entries.add(new TablistEntry(" " + crystal.getColour() + crystal.getDisplayName() + "<f>: " + CrystalNucleus.state(player, crystal).label(),
                    TablistSkinRegistry.GRAY));
        }
        return fillRestWithGray(new ArrayList<>(entries));
    }
}
