package dev.daweapon.type.hub.gui.elizabeth;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.PaginatedView;
import dev.daweapon.type.generic.gui.v2.StatefulPaginatedView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.firesale.FireSale;
import dev.daweapon.type.skyblockgeneric.firesale.FireSaleCatalogue;
import dev.daweapon.type.skyblockgeneric.firesale.FireSaleService;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GUIFireSaleHistory extends StatefulPaginatedView<FireSale, GUIFireSaleHistory.State> {
    private static final List<String> SORTS = List.of("Latest", "Oldest", "Highest Price", "Lowest Price", "Highest Amount", "Lowest Amount");

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, _) -> "Previous Fire Sales ("
                + (Math.min(state.page(), pageCount(state) - 1) + 1) + "/" + pageCount(state) + ")", InventoryType.CHEST_6_ROW);
    }

    private static int pageCount(State state) {
        return Math.max(1, (int) Math.ceil(state.items().size() / (double) DEFAULT_SLOTS.length));
    }

    @Override
    public State initialState() {
        return new State(sorted(0), 0, 0);
    }

    private static List<FireSale> sorted(int sort) {
        List<FireSale> sales = new ArrayList<>(FireSaleCatalogue.all().values());
        switch (sort) {
            case 0 -> java.util.Collections.reverse(sales);
            case 2 -> sales.sort(Comparator.comparingInt(FireSale::gems).reversed());
            case 3 -> sales.sort(Comparator.comparingInt(FireSale::gems));
            case 4 -> sales.sort(Comparator.comparingLong(FireSale::stock).reversed());
            case 5 -> sales.sort(Comparator.comparingLong(FireSale::stock));
            default -> {}
        }
        return sales;
    }

    @Override
    protected int[] getPaginatedSlots() {
        return DEFAULT_SLOTS;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.backOrClose(layout, 49, ctx);
        layout.slot(50, (s, _) -> sortButton(s.sort()), (_, c) -> c.session(Object.class).updateUnchecked(s -> {
            State current = (State) s;
            int next = (current.sort() + 1) % SORTS.size();
            return new State(sorted(next), 0, next);
        }));
    }

    private static ItemStack.Builder sortButton(int selected) {
        StringBuilder text = new StringBuilder("<a>Sort\n");
        for (int i = 0; i < SORTS.size(); i++) {
            text.append(i == selected ? "<a>▶ " : "<7>").append(SORTS.get(i)).append('\n');
        }
        return ItemStacks.item(Material.HOPPER, text.append("\n<e>Click to switch!").toString());
    }

    @Override
    protected ItemStack.Builder renderItem(FireSale sale, int index, HypixelPlayer player) {
        ItemStack.Builder stack = new NonPlayerItemUpdater(FireSaleService.buildItem(sale.items().getFirst())).getUpdatedItem();
        String stock = sale.unlimited() ? "Unlimited" : StringUtility.commaify((int) sale.stock()) + "x";
        String action = player.getRank().isStaff() ? "<e>Click to give yourself this Fire Sale item!" : "<8>Past Fire Sales never return.";
        return ItemStacks.appendLore(stack, " \n<7>Cost: <a>" + StringUtility.commaify(sale.gems())
                + " SkyBlock Gems\n<7>Items offered: <c><l>" + stock + "\n \n" + action);
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, FireSale sale, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (!player.getRank().isStaff()) {
            player.sendMessage("<c>Past Fire Sales never return.");
            return;
        }
        for (String itemId : sale.items()) {
            String reason = sale.unavailableReason(itemId);
            if (reason != null) {
                player.sendMessage("<c>{}: {}", sale.name(), reason);
                continue;
            }
            player.addAndUpdateItem(FireSaleService.buildItem(itemId));
            player.sendMessage("<a>Gave you <6>{} <a>from the {} Fire Sale.", sale.name(), sale.id());
        }
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, FireSale sale) {
        return false;
    }

    public record State(List<FireSale> items, int page, int sort) implements PaginatedView.PaginatedState<FireSale> {
        @Override
        public State withPage(int page) {
            return new State(items, page, sort);
        }

        @Override
        public State withItems(List<FireSale> items) {
            return new State(items, page, sort);
        }
    }
}
