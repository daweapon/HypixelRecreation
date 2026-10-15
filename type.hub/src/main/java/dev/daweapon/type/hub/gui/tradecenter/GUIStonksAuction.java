package dev.daweapon.type.hub.gui.tradecenter;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.leaderboard.LeaderboardService;
import dev.daweapon.type.skyblockgeneric.stonks.StonksAuction;
import dev.daweapon.type.skyblockgeneric.stonks.StonksAuctionTier;
import dev.daweapon.type.skyblockgeneric.stonks.StonksAuctions;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.CoinAmountParser;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;

/** Richard's Stonks Auction menu (wiki Richard/UI). */
public final class GUIStonksAuction implements StatefulView<GUIStonksAuction.State> {
    private static final int AUCTION_SLOT = 11;
    private static final int CLAIM_SLOT = 15;
    private static final int CLOSE_SLOT = 31;

    @Override
    public State initialState() {
        return new State(-1, new EnumMap<>(StonksAuctionTier.class));
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Stonks Auction", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void onOpen(State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        StonksAuctions.finaliseEnded(player);
        String key = StonksAuction.current().leaderboardKey();
        UUID bidder = StonksAuctions.bidder(player);
        Thread.startVirtualThread(() -> {
            int placement = LeaderboardService.getPlayerRank(key, bidder);
            Map<StonksAuctionTier, Double> cutoffs = new EnumMap<>(StonksAuctionTier.class);
            for (StonksAuctionTier tier : StonksAuctionTier.values()) {
                if (tier.isOpenEnded()) continue;
                Double cutoff = LeaderboardService.scoreAtRank(key, tier.getLowestPlacement());
                if (cutoff != null) cutoffs.put(tier, cutoff);
            }
            State loaded = new State(placement, cutoffs);
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                var session = ctx.session(State.class);
                if (!session.isClosed()) session.setState(loaded);
            });
        });
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Components.fill(layout);
        layout.autoUpdating(AUCTION_SLOT, (s, c) -> auctionItem(player, s), (click, c) -> promptBid(player),
                Duration.ofSeconds(1));
        layout.slot(CLAIM_SLOT, (s, c) -> claimItem(player), (click, c) -> {
            if (StonksAuctions.claim(player) == 0) player.sendMessage("<c>You currently have no items to claim!");
            c.session(State.class).refresh();
        });
        Components.close(layout, CLOSE_SLOT);
    }

    private ItemStack.Builder auctionItem(SkyBlockPlayer player, State state) {
        StonksAuction auction = StonksAuction.current();
        List<Text> lore = new ArrayList<>(Text.of("""
                <7>The amount of <5>Stock of Stonks <7>you get depends on
                <7>your placement in the <d>Stonks Auction<7>! The more coins
                <7>you bid the higher you'll place!
                """).lines());
        for (StonksAuctionTier tier : StonksAuctionTier.values()) {
            lore.add(Text.of("<7>▶ <c><l>{}<7> - <5>Stock of Stonks <8>x{}", tier.getLabel(), tier.getStocks()));
            if (tier.isOpenEnded()) continue;
            double cutoff = Math.max(StonksAuction.MINIMUM_BID, state.cutoffs().getOrDefault(tier, 0D));
            lore.add(Text.of("<7>   Minimum Bid: <6>{} Coins", StringUtility.commaify((long) cutoff)));
        }
        lore.add(Text.empty());
        long bid = StonksAuctions.currentBid(player);
        lore.add(bid > 0 ? Text.of("<7>Your current bid: <6>{} Coins", StringUtility.commaify(bid))
                : Text.of("<7>Your current bid: <8>None"));
        lore.add(bid > 0 && state.placement() > 0 ? Text.of("<7>Your current position: <e>#{}", StringUtility.commaify(state.placement()))
                : Text.of("<7>Your current position: <8>None"));
        if (StonksAuctions.isRunning()) {
            lore.add(Text.of("<7>Auction ends in <a>{}", formatRemaining(auction.millisRemaining())));
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to bid!"));
        } else {
            lore.add(Text.empty());
            lore.add(Text.of("<c>The Stonks Auction only runs while"));
            lore.add(Text.of("<6>Diaz <c>is Mayor!"));
        }
        return ItemStacks.item(Material.PAPER, 1, Text.of("<d>Stonks Auction"), lore);
    }

    private ItemStack.Builder claimItem(SkyBlockPlayer player) {
        int stocks = StonksAuctions.profile(player).claimableStocks();
        String header = """
                <a>Claim Auctions
                <7>Once you have participated in a
                <d>Stonks Auction<7>, you can claim your
                <7>items here.

                """;
        if (stocks == 0) {
            return ItemStacks.item(Material.MAP, 1, header + "<c>You currently have no items to claim!");
        }
        return ItemStacks.item(Material.FILLED_MAP, 1, header + "<7>Ready to claim: <5>Stock of Stonks <8>x{}\n\n<e>Click to claim!", stocks);
    }

    private void promptBid(SkyBlockPlayer player) {
        if (!StonksAuctions.isRunning()) {
            player.sendMessage("<c>The Stonks Auction only runs while Diaz is Mayor!");
            return;
        }
        new HypixelSignGUI(player).open(new String[]{"Enter your bid", "in coins"})
                .thenAccept(input -> MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    if (!player.isOnline() || input == null || input.isBlank()) return;
                    OptionalLong amount = CoinAmountParser.parse(input);
                    if (amount.isEmpty()) {
                        player.sendMessage("<c>That isn't a valid amount of coins!");
                        return;
                    }
                    StonksAuctions.bid(player, amount.getAsLong()).ifPresent(player::sendMessage);
                    player.openView(new GUIStonksAuction());
                }));
    }

    private static String formatRemaining(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0) return String.format("%dh %02dm", hours, minutes);
        return String.format("%02dm %ds", minutes, seconds % 60);
    }

    public record State(int placement, Map<StonksAuctionTier, Double> cutoffs) {}
}
