package dev.daweapon.type.mainlobby.tab;

import net.kyori.adventure.text.Component;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointRank;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// Tablist module listing online players in the Main Lobby, paginated across multiple module instances.
public class MainLobbyPlayersOnlineModule extends TablistModule {
    public int page;

    public MainLobbyPlayersOnlineModule(int page) {
        this.page = page;
    }

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer player) {
        Locale l = player.getLocale();
        List<HypixelPlayer> players = HypixelGenericLoader.getLoadedPlayers();

        ArrayList<TablistEntry> entries = new ArrayList<>(List.of(
                centered(Text.key("tablist.module.players", players.size()), l, TablistSkinRegistry.GREEN)
        ));

        List<HypixelPlayer> toShow = new ArrayList<>();

        // Sort players by their rank ordinal in reverse
        players.sort((o1, o2) -> o2.getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().ordinal()
            - o1.getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().ordinal());
        Collections.reverse(players);

        // In chunks of 19, load the players into the toShow list.
        // If the page is 1, then use the first 19, if the page is 2, then use the second set of 19, etc.
        for (int i = 0; i < players.size(); i++) {
            if (i >= (page - 1) * 19 && i < page * 19) {
                toShow.add(players.get(i));
            }
        }

        for (int x = 0; x < 19; x++) {
            if (x >= toShow.size()) {
                entries.add(getGrayEntry());
                continue;
            }

            HypixelPlayer tablistPlayer = toShow.get(x);
            entries.add(new TablistEntry(Text.of("{}", tablistPlayer.getRankDisplayName()), TablistSkinRegistry.GRAY));
        }

        return entries;
    }
}
