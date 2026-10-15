package dev.daweapon.type.hub.gui.tradecenter;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.darkauction.MidasPricePaid;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.CoinAmountParser;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Zarina's Midas Anvil (wiki Zarina/UI). The weapon is picked from the player's inventory and
 * upgraded in place, so it never sits in the menu where a closed window or crash could lose it.
 */
public final class GUIMidasAnvil implements StatefulView<GUIMidasAnvil.State> {
    public static final int STOCK_COST = 3;
    private static final int RESULT_SLOT = 13;
    private static final int ANVIL_SLOT = 22;
    private static final int WEAPON_SLOT = 29;
    private static final int PRICE_SLOT = 33;
    private static final List<Integer> WEAPON_PANES = List.of(11, 12, 20);
    private static final List<Integer> PRICE_PANES = List.of(14, 15, 24);
    private static final List<Integer> BOTTOM_ROW = List.of(45, 46, 47, 48, 50, 51, 52, 53);
    private static final int CLOSE_SLOT = 49;
    private static final int MAIN_INVENTORY_SIZE = 36;

    @Override
    public State initialState() {
        return State.EMPTY;
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Midas Anvil", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Components.fill(layout);
        layout.filler(BOTTOM_ROW, ItemStacks.filler(Material.RED_STAINED_GLASS_PANE));
        Components.close(layout, CLOSE_SLOT);

        Material weaponPane = state.hasWeapon() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        layout.slots(WEAPON_PANES, (s, c) -> ItemStacks.item(weaponPane, 1, """
                <6>Midas Weapon to Upgrade
                <7>The Midas Weapon you want to
                <7>upgrade should be placed in the slot
                <7>on this side."""));
        Material pricePane = state.price() > 0 ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        layout.slots(PRICE_PANES, (s, c) -> ItemStacks.item(pricePane, 1, """
                <6>Coins
                <7>Set the price paid for your <6>Midas
                <7>weapon."""));

        if (state.hasWeapon()) {
            layout.slot(WEAPON_SLOT, (s, c) -> state.snapshot().builder(),
                    (click, c) -> c.session(State.class).setState(state.withWeapon(-1, ItemStack.AIR)));
        } else {
            layout.slot(WEAPON_SLOT, ItemStack.AIR.builder());
        }
        layout.slot(PRICE_SLOT, (s, c) -> priceButton(state), (click, c) -> promptPrice(player, state));
        layout.slot(RESULT_SLOT, (s, c) -> result(player, state));
        layout.slot(ANVIL_SLOT, (s, c) -> anvil(player, state), (click, c) -> upgrade(player, c, state));
    }

    private ItemStack.Builder priceButton(State state) {
        String price = state.price() > 0 ? "<6>" + StringUtility.commaify(state.price()) + " Coins" : "<8>None";
        return ItemStacks.item(Material.GOLD_INGOT, 1, """
                <6>Set Price
                <7>Set the price paid for your <6>Midas
                <7>weapon.

                <7>Set price: {}

                <e>Click to set price!""", price);
    }

    private ItemStack.Builder result(SkyBlockPlayer player, State state) {
        if (problem(player, state, false).isPresent()) {
            return ItemStacks.item(Material.BARRIER, 1, """
                    <c>Midas Anvil
                    <7>Place a Midas weapon in the left slot
                    <7>and set the price on the right!""");
        }
        SkyBlockItem preview = new SkyBlockItem(state.snapshot());
        MidasPricePaid.set(preview, state.price());
        return preview.getItemStack().builder();
    }

    private ItemStack.Builder anvil(SkyBlockPlayer player, State state) {
        Optional<String> problem = problem(player, state, false);
        if (problem.isPresent()) {
            return ItemStacks.item(Material.ANVIL, 1, """
                    <a>Increase Price
                    <7>Use the button on the right to set
                    <7>the price paid for the <6>Midas <7>weapon
                    <7>on the left.""");
        }
        long difference = state.price() - MidasPricePaid.of(new SkyBlockItem(state.snapshot()));
        return ItemStacks.item(Material.ANVIL, 1, """
                <a>Increase Price
                <7>Raise the price paid for this weapon
                <7>to <6>{} Coins<7>.

                <7>Cost
                <6>{} Coins
                <5>Stock of Stonks <8>x{}

                <e>Click to upgrade!""",
                StringUtility.commaify(state.price()), StringUtility.commaify(difference), STOCK_COST);
    }

    private Optional<String> problem(SkyBlockPlayer player, State state, boolean checkCosts) {
        if (!state.hasWeapon() || state.price() <= 0) {
            return Optional.of("<c>Place a Midas weapon in the left slot and set the price on the right!");
        }
        if (!player.getInventory().getItemStack(state.slot()).equals(state.snapshot())) {
            return Optional.of("<c>The weapon in your inventory changed, place it again!");
        }
        long current = MidasPricePaid.of(new SkyBlockItem(state.snapshot()));
        if (state.price() <= current) return Optional.of("<c>The new price must be higher than the price already paid!");
        if (!checkCosts) return Optional.empty();
        if (player.getCoins() < state.price() - current) return Optional.of("<c>You don't have enough coins!");
        if (player.getAmountInInventory(ItemType.STOCK_OF_STONKS) < STOCK_COST) {
            return Optional.of("<c>You need <5>Stock of Stonks <8>x" + STOCK_COST + " <c>to do this!");
        }
        return Optional.empty();
    }

    private void upgrade(SkyBlockPlayer player, ViewContext ctx, State state) {
        Optional<String> problem = problem(player, state, true);
        if (problem.isPresent()) {
            player.sendMessage(problem.get());
            return;
        }
        long difference = state.price() - MidasPricePaid.of(new SkyBlockItem(state.snapshot()));
        if (!player.removeItemFromPlayer(ItemType.STOCK_OF_STONKS, STOCK_COST)) {
            player.sendMessage("<c>You need <5>Stock of Stonks <8>x" + STOCK_COST + " <c>to do this!");
            return;
        }
        player.removeCoins(difference);
        player.updateItemInSlot(state.slot(), item -> MidasPricePaid.set(item, state.price()));
        player.playSuccessSound();
        player.sendMessage("<a>The price paid for your Midas weapon is now <6>" + StringUtility.commaify(state.price()) + " Coins<a>!");
        ctx.session(State.class).setState(new State(state.slot(), player.getInventory().getItemStack(state.slot()), 0));
    }

    private void promptPrice(SkyBlockPlayer player, State state) {
        new HypixelSignGUI(player).open(new String[]{"Enter the price", "paid in coins"})
                .thenAccept(input -> MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    if (!player.isOnline()) return;
                    State next = state;
                    if (input != null && !input.isBlank()) {
                        OptionalLong price = CoinAmountParser.parse(input);
                        if (price.isEmpty()) {
                            player.sendMessage("<c>That isn't a valid amount of coins!");
                        } else if (price.getAsLong() > MidasPricePaid.MIDAS_SWORD_CAP) {
                            player.sendMessage("<c>The price paid can't go above <6>"
                                    + StringUtility.commaify(MidasPricePaid.MIDAS_SWORD_CAP) + " Coins<c>!");
                        } else {
                            next = state.withPrice(price.getAsLong());
                        }
                    }
                    player.openView(new GUIMidasAnvil(), next);
                }));
    }

    @Override
    public boolean onBottomClick(ClickContext<State> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int slot = click.slot();
        if (slot < 0 || slot >= MAIN_INVENTORY_SIZE) return false;
        if (!(click.click() instanceof Click.Left || click.click() instanceof Click.Right)) return false;
        ItemStack stack = player.getInventory().getItemStack(slot);
        if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) return false;
        ItemType type = new SkyBlockItem(stack).getItemType();
        if (type == ItemType.MIDAS_STAFF) {
            player.sendMessage("<c>The Midas Staff's ability isn't in this server yet, so Zarina can't upgrade it!");
            return false;
        }
        if (type != ItemType.MIDAS_SWORD) {
            player.sendMessage("<c>Zarina can only upgrade Midas weapons!");
            return false;
        }
        ctx.session(State.class).setState(click.state().withWeapon(slot, stack));
        return false;
    }

    public record State(int slot, ItemStack snapshot, long price) {
        static final State EMPTY = new State(-1, ItemStack.AIR, 0);

        boolean hasWeapon() {
            return slot >= 0 && !snapshot.isAir();
        }

        State withWeapon(int slot, ItemStack snapshot) {
            return new State(slot, snapshot, price);
        }

        State withPrice(long price) {
            return new State(slot, snapshot, price);
        }
    }
}
