package dev.daweapon.type.hub.gui.elizabeth;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
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
import java.util.List;

public class GUIFireSaleHistory extends StatefulPaginatedView<FireSale, GUIFireSaleHistory.State> {
    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Fire Sale History", InventoryType.CHEST_6_ROW);
    }

    @Override
    public State initialState() {
        List<FireSale> newestFirst = new ArrayList<>(FireSaleCatalogue.all().values());
        java.util.Collections.reverse(newestFirst);
        return new State(newestFirst, 0);
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
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    @Override
    protected ItemStack.Builder renderItem(FireSale sale, int index, HypixelPlayer player) {
        ItemStack.Builder stack = new NonPlayerItemUpdater(FireSaleService.buildItem(sale.items().getFirst())).getUpdatedItem();
        String stock = sale.unlimited() ? "Unlimited" : StringUtility.commaify((int) sale.stock());
        String action = player.getRank().isStaff() ? "<e>Click to give yourself this Fire Sale item!" : "<8>Past Fire Sales never return.";
        return ItemStacks.appendLore(stack, " \n<7>Fire Sale: <e>" + sale.id() + "\n<7>Cost: <a>" + StringUtility.commaify(sale.gems())
                + " Gems\n<7>Stock: <a>" + stock + "\n \n" + action);
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

    public record State(List<FireSale> items, int page) implements PaginatedView.PaginatedState<FireSale> {
        @Override
        public State withPage(int page) {
            return new State(items, page);
        }

        @Override
        public State withItems(List<FireSale> items) {
            return new State(items, page);
        }
    }
}
