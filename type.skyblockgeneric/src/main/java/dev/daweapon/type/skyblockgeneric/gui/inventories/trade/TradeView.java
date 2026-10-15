package dev.daweapon.type.skyblockgeneric.gui.inventories.trade;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.trade.TradeManager;
import dev.daweapon.type.skyblockgeneric.trade.TradeOffer;
import dev.daweapon.type.skyblockgeneric.trade.TradeSession;
import dev.daweapon.type.skyblockgeneric.trade.TradeAllowance;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.CoinAmountParser;

import java.time.Duration;
import java.util.List;
import java.util.OptionalLong;

/** The trade menu from the wiki's Trading/UI: your offer on the left, theirs on the right. */
public final class TradeView implements View<TradeView.State> {
    private static final int[] OWN_SLOTS = {0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30};
    private static final int[] OTHER_SLOTS = {5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35};
    private static final List<Integer> DIVIDER = List.of(4, 13, 22, 31, 40, 49);
    private static final int OWN_COINS_SLOT = 36;
    private static final int OTHER_COINS_SLOT = 41;
    private static final int COINS_BUTTON_SLOT = 45;
    private static final int DEAL_SLOT = 48;
    private static final int STATUS_SLOT = 50;
    private static final int MAIN_INVENTORY_SIZE = 36;

    private final TradeSession session;

    public TradeView(TradeSession session) {
        this.session = session;
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText((state, ctx) -> Text.of("You                  {}",
                session.otherOffer(player(ctx)).getPlayer().getUsername()), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = player(ctx);
        TradeOffer own = session.offerOf(player);
        TradeOffer other = session.otherOffer(player);

        layout.filler(DIVIDER, ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        clear(layout, OWN_SLOTS);
        clear(layout, OTHER_SLOTS);
        clear(layout, new int[]{OWN_COINS_SLOT, OTHER_COINS_SLOT});
        layoutOffer(layout, own, OWN_SLOTS, true);
        layoutOffer(layout, other, OTHER_SLOTS, false);
        layoutCoinLump(layout, own, OWN_COINS_SLOT, true);
        layoutCoinLump(layout, other, OTHER_COINS_SLOT, false);

        layout.slot(COINS_BUTTON_SLOT, (s, c) -> ItemStacks.item(Material.GOLD_NUGGET, 1, """
                        <6>Coins Transaction
                        <7>Daily limit: <a>{}<7>/{}

                        <e>Click to add gold!""",
                StringUtility.shortenNumber(TradeAllowance.sentToday(player)),
                StringUtility.shortenNumber(TradeAllowance.dailyLimit(player))),
                (click, c) -> promptCoins(player, own));
        layout.autoUpdating(DEAL_SLOT, (s, c) -> dealButton(own), (click, c) -> TradeManager.dealClicked(session, player),
                Duration.ofMillis(250));
        layout.slot(STATUS_SLOT, (s, c) -> status(other));
    }

    private static void clear(ViewLayout<State> layout, int[] slots) {
        for (int slot : slots) layout.slot(slot, ItemStack.AIR.builder());
    }

    private void layoutOffer(ViewLayout<State> layout, TradeOffer offer, int[] slots, boolean own) {
        List<TradeOffer.HeldItem> items = List.copyOf(offer.getItems());
        for (int index = 0; index < items.size() && index < slots.length; index++) {
            TradeOffer.HeldItem held = items.get(index);
            if (!own) {
                layout.slot(slots[index], (s, c) -> held.stack().builder());
                continue;
            }
            layout.slot(slots[index], (s, c) -> held.stack().builder(),
                    (click, c) -> session.withdrawItem(offer.getPlayer(), held));
        }
    }

    private void layoutCoinLump(ViewLayout<State> layout, TradeOffer offer, int slot, boolean own) {
        long coins = offer.getCoins();
        if (coins <= 0) return;
        Material material = coins >= 10_000_000 ? Material.GOLD_BLOCK : coins >= 100_000 ? Material.GOLD_INGOT : Material.GOLD_NUGGET;
        String lore = own ? "<7>Lump-sum amount\n\n<e>Click to remove!" : "<7>Lump-sum amount";
        layout.slot(slot, (s, c) -> ItemStacks.item(material, 1, "<6>{} coins\n" + lore, StringUtility.commaify(coins)),
                (click, c) -> {
                    if (own) session.withdrawCoins(offer.getPlayer());
                });
    }

    private ItemStack.Builder dealButton(TradeOffer own) {
        if (own.isAccepted()) {
            return ItemStacks.item(Material.LIME_TERRACOTTA, 1, """
                    <a>Deal accepted!
                    <7>You accepted the trade.
                    <7>Waiting for the other player...""");
        }
        long cooldown = session.cooldownRemainingMillis();
        if (cooldown > 0) {
            return ItemStacks.item(Material.GRAY_TERRACOTTA, 1, """
                    <e>Deal timer!
                    <7>The trade changed recently.
                    <7>Review it before accepting.

                    <c>Can accept in {}s""", (cooldown + 999) / 1000);
        }
        if (own.isWarned()) {
            return ItemStacks.item(Material.RED_TERRACOTTA, 1, """
                    <c>Warning!
                    <7>You are about to give items or
                    <7>coins away for nothing!

                    <e>Click again to accept the trade!""");
        }
        return ItemStacks.item(Material.GREEN_TERRACOTTA, 1, """
                <e>Deal!
                <7>All trades are final and
                <7>cannot be reverted.

                <a>Make sure to review the
                <a>trade before accepting.

                <e>Click to accept the trade!""");
    }

    private ItemStack.Builder status(TradeOffer other) {
        String name = other.getPlayer().getUsername();
        if (other.isAccepted()) {
            return ItemStacks.item(Material.LIME_DYE, 1, """
                    <a>Other player confirmed!
                    <7>Trading with {}.
                    <7>Waiting for you to confirm.""", name);
        }
        return ItemStacks.item(Material.GRAY_DYE, 1, """
                <e>New deal
                <7>Trading with {}.""", name);
    }

    private void promptCoins(SkyBlockPlayer player, TradeOffer own) {
        if (session.isFinished()) return;
        own.setAwaitingCoinInput(true);
        new HypixelSignGUI(player).open(new String[]{"Enter the amount", "of coins to add"})
                .thenAccept(input -> MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    if (session.isFinished() || !player.isOnline()) {
                        own.setAwaitingCoinInput(false);
                        return;
                    }
                    TradeManager.openView(own, session);
                    own.setAwaitingCoinInput(false);
                    if (input == null || input.isBlank()) return;
                    OptionalLong amount = CoinAmountParser.parse(input);
                    if (amount.isEmpty()) {
                        player.sendMessage("<c>That isn't a valid amount of coins!");
                        return;
                    }
                    session.addCoins(player, amount.getAsLong()).ifPresent(player::sendMessage);
                }));
    }

    @Override
    public boolean onBottomClick(ClickContext<State> click, ViewContext ctx) {
        SkyBlockPlayer player = player(ctx);
        int slot = click.slot();
        if (slot < 0 || slot >= MAIN_INVENTORY_SIZE) return false;
        if (!(click.click() instanceof Click.Left || click.click() instanceof Click.Right)) return false;
        if (player.getInventory().getItemStack(slot).isAir()) return false;
        session.offerItem(player, slot).ifPresent(player::sendMessage);
        return false;
    }

    @Override
    public void onClose(State state, ViewContext ctx, ViewSession.CloseReason reason) {
        TradeManager.onViewClosed(session, player(ctx), ctx.session(State.class));
    }

    private static SkyBlockPlayer player(ViewContext ctx) {
        return (SkyBlockPlayer) ctx.player();
    }

    public record State() {}
}
