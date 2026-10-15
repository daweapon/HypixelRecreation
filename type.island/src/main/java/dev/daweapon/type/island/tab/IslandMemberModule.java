package dev.daweapon.type.island.tab;

import net.kyori.adventure.text.Component;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointRank;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// Tablist module listing the Island's online co-op members.
public class IslandMemberModule extends TablistModule {

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        Locale l = player.getLocale();
        ArrayList<SkyBlockPlayer> toShow;
        if (player.isCoop())
            toShow = new ArrayList<>(player.getSkyBlockIsland().getCoop().getOnlineMembers());
        else
            toShow = new ArrayList<>(Collections.singletonList(player));

        ArrayList<TablistEntry> entries = new ArrayList<>(List.of(
                centered(Text.key("tablist.module.island", toShow.size()), l, TablistSkinRegistry.CYAN)
        ));

        toShow.sort((o1, o2) -> o2.getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().ordinal()
                - o1.getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().ordinal());
        Collections.reverse(toShow);

        for (int x = 0; x < 19; x++) {
            if (x >= toShow.size()) {
                entries.add(getGrayEntry());
                continue;
            }

            SkyBlockPlayer tablePlayer = toShow.get(x);
            entries.add(new TablistEntry(Text.of("{}", tablePlayer.getRankDisplayName()), TablistSkinRegistry.GRAY));
        }

        fillRestWithGray(entries);

        return entries;
    }
}
