package dev.daweapon.type.island.tab;

import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.tab.TabLine;
import dev.daweapon.type.skyblockgeneric.tab.TabPlayerBlock;
import dev.daweapon.type.skyblockgeneric.tab.TabPlayers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IslandPlayersBlock implements TabPlayerBlock {
    private static final int MAX_MEMBERS = 12;

    @Override
    public List<TabLine> lines(SkyBlockPlayer viewer) {
        boolean coop = viewer.isCoop();
        List<SkyBlockPlayer> members = TabPlayers.byRank(coop
                ? new ArrayList<>(viewer.getSkyBlockIsland().getCoop().getOnlineMembers())
                : new ArrayList<>(Collections.singletonList(viewer)));
        int guests = Math.max(0, SkyBlockGenericLoader.getLoadedPlayers().size() - members.size());

        List<TabLine> lines = new ArrayList<>();
        lines.add(TabPlayers.header(coop ? "tablist.module.coop" : "tablist.module.island", viewer,
                members.size(), TablistSkinRegistry.CYAN));
        members.stream().limit(MAX_MEMBERS).forEach(member -> lines.add(TabPlayers.name(member, ignored -> "")));
        lines.add(null);
        lines.add(TabPlayers.header("tablist.module.guests_count", viewer, guests, TablistSkinRegistry.PURPLE));
        return lines;
    }
}
