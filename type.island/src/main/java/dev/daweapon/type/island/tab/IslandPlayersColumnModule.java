package dev.daweapon.type.island.tab;

import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointRank;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class IslandPlayersColumnModule extends TablistModule {
    private static final int MAX_MEMBERS = 12;

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer viewer) {
        SkyBlockPlayer player = (SkyBlockPlayer) viewer;
        Locale locale = player.getLocale();
        List<SkyBlockPlayer> members = player.isCoop()
                ? new ArrayList<>(player.getSkyBlockIsland().getCoop().getOnlineMembers())
                : new ArrayList<>(Collections.singletonList(player));
        members.sort(Comparator.comparingInt((SkyBlockPlayer member) -> member.getDataHandler()
                .get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().ordinal()).reversed());

        List<TablistEntry> entries = new ArrayList<>();
        entries.add(centered(Text.key("tablist.module.island", members.size()), locale, TablistSkinRegistry.CYAN));
        members.stream().limit(MAX_MEMBERS).forEach(member ->
                entries.add(new TablistEntry(Text.of("{}", member.getRankDisplayName()), TablistSkinRegistry.GRAY)));
        entries.add(getGrayEntry());
        entries.add(centered(Text.key("tablist.module.guests"), locale, TablistSkinRegistry.PURPLE));
        return fillRestWithGray(entries);
    }
}
