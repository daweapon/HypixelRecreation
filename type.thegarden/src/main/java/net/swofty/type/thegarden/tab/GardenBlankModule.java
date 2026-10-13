package net.swofty.type.thegarden.tab;

import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.List;

public class GardenBlankModule extends TablistModule {

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer player) {
        List<TablistEntry> entries = new ArrayList<>();
        fillRestWithGray(entries);
        return entries;
    }
}
