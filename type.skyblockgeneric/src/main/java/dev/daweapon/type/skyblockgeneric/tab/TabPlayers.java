package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointRank;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class TabPlayers {
    public static final int MAX_NAMES = 19;

    private TabPlayers() {
    }

    public static TabLine header(String key, SkyBlockPlayer viewer, Object count, dev.daweapon.type.generic.tab.TablistSkin skin) {
        return TabLine.header(TablistModule.getCentered(Text.key(key, count), viewer.getLocale()), skin);
    }

    public static TabLine name(SkyBlockPlayer player, Function<SkyBlockPlayer, String> suffix) {
        return TabLine.of(Text.of("{}", player.getRankDisplayName()).append(suffix.apply(player)));
    }

    public static List<SkyBlockPlayer> byRank(List<SkyBlockPlayer> players) {
        List<SkyBlockPlayer> sorted = new ArrayList<>(players);
        sorted.sort(Comparator.comparingInt((SkyBlockPlayer p) -> p.getDataHandler()
                .get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().ordinal()).reversed());
        return sorted;
    }

    public static List<TabLine> names(SkyBlockPlayer viewer, Function<SkyBlockPlayer, String> suffix) {
        List<SkyBlockPlayer> players = byRank(new ArrayList<>(SkyBlockGenericLoader.getLoadedPlayers()));
        List<TabLine> lines = new ArrayList<>();
        lines.add(header("tablist.module.players", viewer, players.size(), TablistSkinRegistry.GREEN));
        players.stream().limit(MAX_NAMES).forEach(player -> lines.add(name(player, suffix)));
        return lines;
    }
}
