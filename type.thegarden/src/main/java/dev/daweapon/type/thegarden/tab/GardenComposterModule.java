package dev.daweapon.type.thegarden.tab;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.tab.TablistModule.TablistEntry;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.skyblockgeneric.garden.composter.ComposterService;
import dev.daweapon.type.skyblockgeneric.garden.composter.ComposterState;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class GardenComposterModule {
    private GardenComposterModule() {
    }

    static List<TablistEntry> entries(SkyBlockPlayer player) {
        ComposterState state = ComposterService.state(player);
        long seconds = state.running() ? (long) Math.ceil(state.getRemainingSeconds()) : 0;
        List<TablistEntry> entries = new ArrayList<>();
        entries.add(new TablistEntry(Text.of("<a><l>Composter:"), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.of(" <7>Organic Matter: <a>{}", StringUtility.commaify((long) state.getOrganicMatter())), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.of(" <7>Fuel: <a>{}", StringUtility.commaify((long) state.getFuel())), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.of(" <7>Time Left: <a>{}", String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.of(" <7>Stored Compost: <a>{}", StringUtility.commaify(state.getStoredCompost())), TablistSkinRegistry.GRAY));
        return entries;
    }
}
