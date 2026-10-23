package dev.daweapon.type.skyblockgeneric.gui.inventories.leaderboard;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.leaderboard.SkyBlockBoard;
import dev.daweapon.type.skyblockgeneric.leaderboard.SkyBlockLeaderboards;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.List;

public class GUILeaderboardView extends StatelessView {
    private static final int[] SLOTS = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24};
    private static final String[] RANK_COLOURS = {"<6>", "<f>", "<c>"};

    private final SkyBlockLeaderboards.Page page;

    public GUILeaderboardView(SkyBlockLeaderboards.Page page) {
        this.page = page;
    }

    public static void open(SkyBlockPlayer player, SkyBlockBoard board) {
        SkyBlockLeaderboards.loadAsync(player, board, page -> player.openView(new GUILeaderboardView(page)));
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(page.board().name(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
        java.util.UUID ownProfile = SkyBlockLeaderboards.currentProfile(viewer);

        for (int i = 0; i < SLOTS.length; i++) {
            if (i >= page.top().size()) break;
            SkyBlockLeaderboards.Row row = page.top().get(i);
            boolean own = row.profileId().equals(ownProfile);
            String colour = i < RANK_COLOURS.length ? RANK_COLOURS[i] : "<a>";
            layout.slot(SLOTS[i], ItemStacks.item(own ? Material.GOLD_BLOCK : Material.PAPER,
                    Text.of(colour + "#{} <f>{}", row.rank(), row.owner()), List.of(
                            Text.of("<7>Profile: <a>{}", row.profileName()),
                            Text.of("<7>{}: <6>{}", unitLabel(), format(row.score())),
                            own ? Text.of("<a>This is you!") : Text.empty())));
        }
        if (page.top().isEmpty()) {
            layout.slot(22, ItemStacks.item(Material.BARRIER, Text.of("<c>Nobody is ranked yet"), List.of(
                    Text.of("<7>Profiles appear here after their"),
                    Text.of("<7>next save."))));
        }

        SkyBlockLeaderboards.Row own = page.own();
        layout.slot(40, ItemStacks.item(Material.PLAYER_HEAD, Text.of("<a>Your Ranking"), own == null
                ? List.of(Text.of("<7>You are not ranked on this board."), Text.of("<8>Staff and empty profiles aren't ranked."))
                : List.of(Text.of("<7>Rank: <6>#{} <8>of {}", own.rank(), page.total()),
                Text.of("<7>{}: <6>{}", unitLabel(), format(own.score())))));
    }

    private String unitLabel() {
        return StringUtility.toNormalCase(page.board().unit());
    }

    private static String format(double score) {
        return score == Math.floor(score) ? StringUtility.commaify((long) score) : String.format("%,.1f", score);
    }
}
