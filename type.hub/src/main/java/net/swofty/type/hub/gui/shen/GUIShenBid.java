package net.swofty.type.hub.gui.shen;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.shen.ShenAuction;
import net.swofty.type.skyblockgeneric.shen.ShenAuctionItem;
import net.swofty.type.skyblockgeneric.shen.ShenAuctions;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.CoinAmountParser;

import java.util.OptionalLong;

/** "Place a Shen Bid" (wiki Damia/UI): bid the minimum, or type a custom bid on a sign. */
public final class GUIShenBid implements StatefulView<GUIShenBid.State> {
    private static final int MINIMUM_BID_SLOT = 12;
    private static final int CUSTOM_BID_SLOT = 14;
    private static final int BACK_SLOT = 31;

    @Override
    public State initialState() {
        return new State(ShenAuctionItem.ARTIFACT_OF_CONTROL, ShenAuction.AUCTION_MINIMUM_BID);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Place a Shen Bid", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Components.fill(layout);
        long current = ShenAuctions.currentBid(player, state.item());
        long minimum = Math.max(state.minimum(), current + 1);

        layout.slot(MINIMUM_BID_SLOT, ItemStacks.item(Material.GOLD_NUGGET, """
                <f>Minimum Bid: <6>{} coins
                <7>You must bid at least this
                <7>amount to be in the top bidders
                <7>list!

                <7>The top bidders list will each
                <7>win this item once the auction
                <7>has ended.

                <e>Click to bid!""", StringUtility.commaify(minimum)), (click, c) -> {
            ShenAuctions.bid(player, state.item(), minimum, state.minimum()).ifPresent(player::sendMessage);
            reopen(player);
        });
        layout.slot(CUSTOM_BID_SLOT, ItemStacks.item(Material.GOLD_INGOT, """
                <6>Custom Bid
                <7>Here you can place a custom
                <7>bid for this auction.

                <7>Keep in mind the bid must be
                <7>enough to be placed in the top
                <7>bidders list!

                <e>Click to place a custom bid!"""), (click, c) -> promptBid(player, state));
        layout.slot(BACK_SLOT, ItemStacks.item(Material.ARROW, "<a>Go Back\n<7>To Shen's Auction"),
                (click, c) -> reopen(player));
    }

    private void promptBid(SkyBlockPlayer player, State state) {
        new HypixelSignGUI(player).open(new String[]{"Enter your bid", "in coins"})
                .thenAccept(input -> MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    if (!player.isOnline() || input == null || input.isBlank()) return;
                    OptionalLong amount = CoinAmountParser.parse(input);
                    if (amount.isEmpty()) {
                        player.sendMessage("<c>That isn't a valid amount of coins!");
                        return;
                    }
                    ShenAuctions.bid(player, state.item(), amount.getAsLong(), state.minimum()).ifPresent(player::sendMessage);
                    reopen(player);
                }));
    }

    private static void reopen(SkyBlockPlayer player) {
        player.openView(new GUIShenAuction());
    }

    public record State(ShenAuctionItem item, long minimum) {}
}
