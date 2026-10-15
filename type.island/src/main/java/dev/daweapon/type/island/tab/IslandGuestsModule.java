package dev.daweapon.type.island.tab;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Tablist module header for the Island's guests section.
public class IslandGuestsModule extends TablistModule {
    @Override
    public List<TablistEntry> getEntries(HypixelPlayer player) {
        Locale l = player.getLocale();
        ArrayList<TablistEntry> entries = new ArrayList<>(List.of(
                centered(Text.key("tablist.module.guests"), l, TablistSkinRegistry.PURPLE)
        ));

        fillRestWithGray(entries);

        return entries;
    }
}
