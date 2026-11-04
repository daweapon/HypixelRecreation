package dev.daweapon.type.skyblockgeneric.gui.inventories.rabbits;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.Chocobits;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public class GUIChocobitTower implements StatefulView<GUIChocobitTower.State> {
    private static final String CHOCOBIT_TEXTURE = "9a815398e7da89b1bc08f646cafc8e7b813da0be0eec0cce6d3eff5207801026";
    private static final int[] CHOCOBIT_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    public record State(int page) {
    }

    @Override
    public State initialState() {
        return new State(0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Chocobit Tower", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);

        layout.slot(4, (s, c) -> {
            ChocolateFactoryData data = ChocolateFactoryHelper.getData((SkyBlockPlayer) c.player());
            return ItemStacks.item(Material.REDSTONE_TORCH, "<c>Chocobits\n"
                    + "<7>There are exactly <a>33,333 <7>Chocobit\n"
                    + "<7>Rabbits, each one labeled #1 to\n"
                    + "<7>#33333.\n"
                    + "<7>Each Chocobit that you own adds\n"
                    + "<6>+0.2x <7>to your Multiplier.\n\n"
                    + "<c><l>WARNING:</l> <7>To keep your Chocobits, you\n"
                    + "<7>need to pay a Coin fee every\n"
                    + "<7>SkyBlock year!\n\n"
                    + "<7>You currently own <a>" + data.getChocobits().size() + " <7>Chocobits.");
        });

        for (int index = 0; index < CHOCOBIT_SLOTS.length; index++) {
            int position = state.page() * CHOCOBIT_SLOTS.length + index;
            layout.slot(CHOCOBIT_SLOTS[index], (s, c) -> {
                List<Integer> owned = owned((SkyBlockPlayer) c.player());
                if (position >= owned.size()) return ItemStack.builder(Material.AIR);
                int number = owned.get(position);
                int paidThrough = ChocolateFactoryHelper.getData((SkyBlockPlayer) c.player()).getChocobits().getOrDefault(number, 0);
                return ItemStacks.head(CHOCOBIT_TEXTURE, chocobitLore(number, paidThrough));
            }, (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                List<Integer> owned = owned(player);
                if (position >= owned.size()) return;
                payFor(player, owned.get(position));
                c.session(State.class).refresh();
            });
        }

        layout.slot(49, (s, c) -> ItemStacks.item(Material.GOLD_INGOT, "<6>Pay All Fees\n"
                        + "<7>Keep every Chocobit that is due\n"
                        + "<7>this year for another SkyBlock year.\n\n"
                        + "<7>Cost: <6>" + Chocobits.formatCoins(Chocobits.EXTENSION_COINS) + " Coins <7>each\n\n"
                        + "<e>Click to pay!"),
                (click, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    int paid = Chocobits.extendAll(player);
                    player.sendMessage(paid == 0 ? "<c>No Chocobit fees could be paid!"
                            : "<a>You paid the fee for <6>" + paid + " <a>Chocobits!");
                    c.session(State.class).refresh();
                });

        if (state.page() > 0) {
            layout.slot(45, (s, c) -> ItemStacks.item(Material.ARROW, "<a>Previous Page"),
                    (click, c) -> c.session(State.class).update(current -> new State(current.page() - 1)));
        }
        layout.slot(53, (s, c) -> {
            if (owned((SkyBlockPlayer) c.player()).size() <= (state.page() + 1) * CHOCOBIT_SLOTS.length) {
                return ItemStacks.item(Material.BLACK_STAINED_GLASS_PANE, " ");
            }
            return ItemStacks.item(Material.ARROW, "<a>Next Page");
        }, (click, c) -> {
            if (owned((SkyBlockPlayer) c.player()).size() <= (state.page() + 1) * CHOCOBIT_SLOTS.length) return;
            c.session(State.class).update(current -> new State(current.page() + 1));
        });

        Components.back(layout, 48, ctx);
    }

    private static List<Integer> owned(SkyBlockPlayer player) {
        return ChocolateFactoryHelper.getData(player).getChocobits().keySet().stream().sorted().toList();
    }

    private static String chocobitLore(int number, int paidThrough) {
        int year = SkyBlockCalendar.getYear();
        String status = paidThrough > year
                ? "<a>Fee paid for Year " + (year + 1) + "!"
                : "<c>Leaves at the end of Year " + year + "!\n\n<7>Cost: <6>" + Chocobits.formatCoins(Chocobits.EXTENSION_COINS)
                + " Coins\n\n<e>Click to keep it for another year!";
        return "<c>" + Chocobits.label(number) + "\n"
                + "<7>Adds <6>+0.2x <7>to your <6>Chocolate\n"
                + "<6>Factory <7>Multiplier.\n\n"
                + status + "\n\n"
                + "<c><l>SPECIAL RABBIT";
    }

    private static void payFor(SkyBlockPlayer player, int number) {
        Integer paidThrough = ChocolateFactoryHelper.getData(player).getChocobits().get(number);
        if (paidThrough == null) return;
        if (paidThrough > SkyBlockCalendar.getYear()) {
            player.sendMessage("<c>You already paid the fee for " + Chocobits.label(number) + "!");
            return;
        }
        if (!Chocobits.extend(player, number)) {
            player.sendMessage("<c>You don't have enough Coins in your purse!");
            return;
        }
        player.sendMessage("<a>You paid <6>" + Chocobits.formatCoins(Chocobits.EXTENSION_COINS) + " Coins <a>to keep <c>"
                + Chocobits.label(number) + "<a>!");
    }
}
