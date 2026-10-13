package net.swofty.type.thegarden.tab;

import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.tab.TablistSkinRegistry;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContest;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterService;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterState;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class GardenComposterModule extends TablistModule {
    private static final long TICK_MILLIS = 50;

    private static String contestLine() {
        long elapsed = SkyBlockCalendar.getElapsed();
        Optional<FarmingContest> active = FarmingContest.active();
        if (active.isPresent()) {
            return "<a>Active <7>(ends in " + clock((active.get().endElapsed() - elapsed) * TICK_MILLIS) + ")";
        }
        FarmingContest next = FarmingContest.upcoming(1).getFirst();
        return "<7>Starts in <a>" + clock((next.startElapsed() - elapsed) * TICK_MILLIS);
    }

    private static String clock(long millis) {
        long seconds = Math.max(0, millis / 1000);
        return seconds >= 60 ? seconds / 60 + "m " + seconds % 60 + "s" : seconds + "s";
    }

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        ComposterState state = ComposterService.state(player);
        long seconds = state.running() ? (long) Math.ceil(state.getRemainingSeconds()) : 0;
        List<TablistEntry> entries = new ArrayList<>();
        entries.add(new TablistEntry(Text.of("<a><l>Composter:"), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.of(" <7>Organic Matter: <a>{}", StringUtility.commaify((long) state.getOrganicMatter())), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.of(" <7>Fuel: <a>{}", StringUtility.commaify((long) state.getFuel())), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.of(" <7>Time Left: <a>{}", String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.of(" <7>Stored Compost: <a>{}", StringUtility.commaify(state.getStoredCompost())), TablistSkinRegistry.GRAY));
        entries.add(getGrayEntry());
        entries.add(new TablistEntry(Text.of("<a><l>Jacob's Contest:"), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.of(" {}", Text.parse(contestLine())), TablistSkinRegistry.GRAY));
        entries.add(getGrayEntry());
        entries.add(new TablistEntry(Text.of("<a><l>Election:"), TablistSkinRegistry.GRAY));
        SkyBlockMayor mayor = ElectionManager.getCurrentMayor();
        entries.add(new TablistEntry(Text.of(" <7>Mayor: <a>{}", mayor == null ? "None" : mayor.getDisplayName()), TablistSkinRegistry.GRAY));
        fillRestWithGray(entries);
        return entries;
    }
}
