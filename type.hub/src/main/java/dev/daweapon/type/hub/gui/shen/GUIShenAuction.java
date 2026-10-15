package dev.daweapon.type.hub.gui.shen;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.leaderboard.LeaderboardService;
import dev.daweapon.type.skyblockgeneric.shen.ShenAuction;
import dev.daweapon.type.skyblockgeneric.shen.ShenAuctionItem;
import dev.daweapon.type.skyblockgeneric.shen.ShenAuctionProfile;
import dev.daweapon.type.skyblockgeneric.shen.ShenAuctions;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Damia's Shen's Auction menu (wiki Damia/UI). */
public final class GUIShenAuction implements StatefulView<GUIShenAuction.State> {
    private static final int[] AUCTION_HEADER_SLOTS = {2, 4, 6};
    private static final int[] AUCTION_SLOTS = {11, 13, 15};
    private static final int[] SPECIAL_HEADER_SLOTS = {21, 23};
    private static final int[] SPECIAL_SLOTS = {30, 32};
    private static final int[] VOLUME_TRADING_SLOTS = {29, 33};
    private static final int CLAIM_ITEMS_SLOT = 48;
    private static final int CLOSE_SLOT = 49;
    private static final int CLAIM_REFUNDS_SLOT = 50;
    private static final String RULE = "<8><m>-----------------</m>";

    @Override
    public State initialState() {
        return new State(ShenAuctions.isVolumeTrading(), new EnumMap<>(ShenAuctionItem.class), new EnumMap<>(ShenAuctionItem.class));
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Shen's Auction", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        ShenAuctions.finaliseEnded(player);
        ShenAuction auction = ShenAuction.current();
        boolean ironman = player.isIronman();
        UUID bidder = ShenAuctions.bidder(player);
        Thread.startVirtualThread(() -> {
            boolean volumeTrading = ShenAuctions.isVolumeTrading() || ShenAuctions.wasVolumeTrading(auction);
            Map<ShenAuctionItem, Long> minimums = new EnumMap<>(ShenAuctionItem.class);
            Map<ShenAuctionItem, Integer> placements = new EnumMap<>(ShenAuctionItem.class);
            for (ShenAuctionItem item : ShenAuctionItem.values()) {
                if (!auction.offers(item, volumeTrading)) continue;
                minimums.put(item, ShenAuctions.minimumBid(auction, item, ironman, volumeTrading));
                placements.put(item, LeaderboardService.getPlayerRank(auction.leaderboardKey(item, ironman), bidder));
            }
            State loaded = new State(volumeTrading, minimums, placements);
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                var session = ctx.session(State.class);
                if (!session.isClosed()) session.setState(loaded);
            });
        });
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        ShenAuction auction = ShenAuction.current();
        Components.fill(layout);

        for (int i = 0; i < AUCTION_HEADER_SLOTS.length; i++) {
            layout.slot(AUCTION_HEADER_SLOTS[i], ItemStacks.item(Material.ORANGE_DYE, """
                    <6>Shen's Auction #{}
                    <8>Requires Museum Milestone 10

                    <7>Every year, Shen is auctioning three
                    <7>special accessories to those he
                    <7>deems popular enough to deserve
                    <7>his items.

                    <e>View below!""", i + 1));
        }
        List<ShenAuctionItem> auctionItems = ShenAuctionItem.of(ShenAuctionItem.Section.AUCTION);
        for (int i = 0; i < AUCTION_SLOTS.length; i++) {
            placeItem(layout, player, auctionItems.get(i), AUCTION_SLOTS[i]);
        }

        for (int slot : SPECIAL_HEADER_SLOTS) {
            layout.slot(slot, ItemStacks.item(Material.CYAN_DYE, """
                    <3>Shen's Special
                    <8>Requires Museum Milestone 20

                    <7>Shen is auctioning two more special
                    <7>edition items to the richest of the
                    <7>rich this year!

                    <e>View below!"""));
        }
        List<ShenAuctionItem> specials = auction.specials();
        for (int i = 0; i < SPECIAL_SLOTS.length; i++) {
            placeSpecial(layout, player, specials.get(i), SPECIAL_SLOTS[i]);
        }
        if (state.volumeTrading()) {
            List<ShenAuctionItem> volumeSpecials = auction.volumeTradingSpecials();
            for (int i = 0; i < VOLUME_TRADING_SLOTS.length; i++) {
                placeSpecial(layout, player, volumeSpecials.get(i), VOLUME_TRADING_SLOTS[i]);
            }
        }

        layout.slot(CLAIM_ITEMS_SLOT, (s, c) -> claimItemsButton(player), (click, c) -> {
            if (ShenAuctions.claimItems(player) == 0) player.sendMessage("<c>You currently have no auctions to claim!");
            c.session(State.class).refresh();
        });
        Components.close(layout, CLOSE_SLOT);
        layout.slot(CLAIM_REFUNDS_SLOT, (s, c) -> claimRefundsButton(player), (click, c) -> {
            if (ShenAuctions.claimRefunds(player) == 0) player.sendMessage("<c>You currently have no bids to claim!");
            c.session(State.class).refresh();
        });
    }

    private void placeSpecial(ViewLayout<State> layout, SkyBlockPlayer player, ShenAuctionItem item, int slot) {
        if (ShenAuctions.canView(player, item)) {
            placeItem(layout, player, item, slot);
            return;
        }
        layout.slot(slot, ItemStacks.item(Material.GRAY_DYE, "<c>???\n<7>Don't worry about this!"), (click, c) -> {
            player.closeInventory();
            List.of("I said don't worry about that!",
                    "I have told you time and time again you aren't popular enough for Shen yet!",
                    "Yet you still try to get through!",
                    "You must not be very fond of rules are you?",
                    "Anyway, unlock Museum Milestone 20 and I MIGHT let you see that!")
                    .forEach(line -> player.sendMessage(ShenAuctions.DAMIA + line));
        });
    }

    private void placeItem(ViewLayout<State> layout, SkyBlockPlayer player, ShenAuctionItem item, int slot) {
        layout.autoUpdating(slot, (s, c) -> auctionItem(player, item, s), (click, c) -> {
            if (!item.isAvailable()) {
                player.sendMessage("<c>" + item.getUnavailableReason());
                return;
            }
            if (click.click() instanceof Click.Right) {
                c.push(new GUIShenTopBids(), new GUIShenTopBids.State(item, click.state().volumeTrading(), List.of()));
                return;
            }
            c.push(new GUIShenBid(), new GUIShenBid.State(item, click.state().minimums().getOrDefault(item, ShenAuction.minimumBid(item))));
        }, Duration.ofSeconds(1));
    }

    private ItemStack.Builder auctionItem(SkyBlockPlayer player, ShenAuctionItem item, State state) {
        boolean ironman = player.isIronman();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(RULE));
        if (!item.isAvailable()) {
            lore.add(Text.of("<c>Not available on this server yet!"));
            lore.add(Text.of("<8>{}", item.getUnavailableReason()));
            lore.add(Text.of(RULE));
            lore.add(Text.of("<7>The top <a>{}<7> bidders win this item!", item.winners(ironman, state.volumeTrading())));
            return ShenItemIcons.icon(player, item, lore);
        }
        long bid = ShenAuctions.currentBid(player, item);
        if (bid > 0) {
            lore.add(Text.of("<7>Your bid: <6>{} coins", StringUtility.commaify(bid)));
            int placement = state.placements().getOrDefault(item, -1);
            if (placement > 0) lore.add(Text.of("<7>Your position: <e>#{}", StringUtility.commaify(placement)));
        } else {
            lore.add(Text.of("<c>You haven't bid!"));
        }
        lore.add(Text.of("<7>Auction ends in: <a>{}", formatRemaining(ShenAuction.current().millisRemaining())));
        lore.add(Text.of(RULE));
        lore.add(Text.of("<7>The top <a>{}<7> bidders win this item!", item.winners(ironman, state.volumeTrading())));
        lore.add(Text.of("<8><o>(Right-click to view top bids!)"));
        lore.add(Text.of(RULE));
        lore.add(Text.of("<e>Click to bid!"));
        return ShenItemIcons.icon(player, item, lore);
    }

    private ItemStack.Builder claimItemsButton(SkyBlockPlayer player) {
        List<ShenAuctionProfile.Entry> won = ShenAuctions.profile(player).claimable(ShenAuctionProfile.Outcome.WON);
        if (won.isEmpty()) {
            return ItemStacks.item(Material.GRAY_DYE, """
                    <c>No Auctions to Claim
                    <7>You currently have no auctions to
                    <7>claim. Once you have won a Shen
                    <7>Auction, you can claim your item here.""");
        }
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>You have won these auctions:"));
        won.forEach(entry -> lore.add(Text.of("<8>- " + entry.getItem().getDisplayName())));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to claim!"));
        return ItemStacks.item(Material.LIME_DYE, 1, Text.of("<a>Auctions to Claim"), lore);
    }

    private ItemStack.Builder claimRefundsButton(SkyBlockPlayer player) {
        long refund = ShenAuctions.profile(player).claimable(ShenAuctionProfile.Outcome.LOST).stream()
                .mapToLong(ShenAuctionProfile.Entry::getBid).sum();
        if (refund == 0) {
            return ItemStacks.item(Material.GRAY_DYE, """
                    <c>No Bids to Claim
                    <7>If you place a bid and do not win an
                    <7>auction, you can claim your
                    <7>currency here.""");
        }
        return ItemStacks.item(Material.GOLD_INGOT, """
                <a>Bids to Claim
                <7>These bids didn't win, so you can
                <7>take your coins back.

                <7>Refund: <6>{} coins

                <e>Click to claim!""", StringUtility.commaify(refund));
    }

    static String formatRemaining(long millis) {
        long seconds = millis / 1000;
        return String.format("%dd %dh %dm %ds", seconds / 86_400, (seconds % 86_400) / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    public record State(boolean volumeTrading, Map<ShenAuctionItem, Long> minimums, Map<ShenAuctionItem, Integer> placements) {}
}
