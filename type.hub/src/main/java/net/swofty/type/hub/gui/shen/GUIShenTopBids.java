package net.swofty.type.hub.gui.shen;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.skyblockgeneric.shen.ShenAuction;
import net.swofty.type.skyblockgeneric.shen.ShenAuctionItem;
import net.swofty.type.skyblockgeneric.shen.ShenAuctions;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.UUID;

/** The current top bids on one of Shen's items; bidders stay anonymous except for your own bid. */
public final class GUIShenTopBids implements StatefulView<GUIShenTopBids.State> {
    private static final int SHOWN = 45;
    private static final int BACK_SLOT = 49;

    @Override
    public State initialState() {
        return new State(ShenAuctionItem.ARTIFACT_OF_CONTROL, false, List.of());
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Top Bids", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        String key = ShenAuction.current().leaderboardKey(state.item(), player.isIronman());
        Thread.startVirtualThread(() -> {
            List<LeaderboardService.LeaderboardEntry> top = LeaderboardService.getTopN(key, SHOWN);
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                var session = ctx.session(State.class);
                if (!session.isClosed()) session.setState(new State(state.item(), state.volumeTrading(), top));
            });
        });
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        UUID own = ShenAuctions.bidder(player);
        int winners = state.item().winners(player.isIronman(), state.volumeTrading());
        Components.fill(layout);
        for (int i = 0; i < state.top().size(); i++) {
            LeaderboardService.LeaderboardEntry entry = state.top().get(i);
            boolean mine = entry.playerUuid().equals(own);
            boolean winning = entry.rank() <= winners;
            String title = (winning ? "<a>#{}" : "<7>#{}") + (mine ? " <e>(You)" : "");
            String status = winning ? "<a>Currently winning!" : "<c>Not in the top {}!";
            layout.slot(i, ItemStacks.item(winning ? Material.GOLD_INGOT : Material.IRON_INGOT, entry.rank(),
                    title + "\n<7>Bid: <6>{} coins\n" + status, entry.rank(), StringUtility.commaify((long) entry.score()), winners));
        }
        layout.slot(BACK_SLOT, ItemStacks.item(Material.ARROW, "<a>Go Back\n<7>To Shen's Auction"),
                (click, c) -> player.openView(new GUIShenAuction()));
    }

    public record State(ShenAuctionItem item, boolean volumeTrading, List<LeaderboardService.LeaderboardEntry> top) {}
}
