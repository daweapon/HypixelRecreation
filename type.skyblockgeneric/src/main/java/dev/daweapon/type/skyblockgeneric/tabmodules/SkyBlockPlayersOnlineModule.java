package dev.daweapon.type.skyblockgeneric.tabmodules;

import net.kyori.adventure.text.Component;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointRank;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class SkyBlockPlayersOnlineModule extends TablistModule {
    public int page;
    private final Function<SkyBlockPlayer, String> nameSuffix;

    public SkyBlockPlayersOnlineModule(int page) {
        this(page, ignored -> "");
    }

    public SkyBlockPlayersOnlineModule(int page, Function<SkyBlockPlayer, String> nameSuffix) {
        this.page = page;
        this.nameSuffix = nameSuffix;
    }


    public List<TablistEntry> getEntries(SkyBlockPlayer player) {
        Locale l = player.getLocale();
        // Real Hypixel's tablist includes the viewer themselves in the player list, not just
        // everyone else - previously excluded here, which made the list look empty whenever you
        // were the only one online despite the header correctly saying "Players (1)".
        List<SkyBlockPlayer> players = new ArrayList<>(SkyBlockGenericLoader.getLoadedPlayers());
        int playerCount = players.size();

        ArrayList<TablistEntry> entries = new ArrayList<>(List.of(
                centered(Text.key("tablist.module.players", playerCount), l, TablistSkinRegistry.GREEN)
        ));

        List<SkyBlockPlayer> toShow = new ArrayList<>();

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

            SkyBlockPlayer tablistPlayer = toShow.get(x);
            entries.add(new TablistEntry(Text.of("{}", tablistPlayer.getRankDisplayName()).append(nameSuffix.apply(tablistPlayer)), TablistSkinRegistry.GRAY));
        }

        return entries;
    }

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer player) {
        return getEntries((SkyBlockPlayer) player);
    }
}
