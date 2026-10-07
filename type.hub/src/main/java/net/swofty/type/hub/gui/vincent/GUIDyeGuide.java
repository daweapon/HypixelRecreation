package net.swofty.type.hub.gui.vincent;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.PaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;

import java.util.List;

public class GUIDyeGuide extends PaginatedView<VincentDye, GUIDyeGuide.State> {

    public record State(List<VincentDye> items, int page) implements PaginatedState<VincentDye> {
        @Override
        public PaginatedState<VincentDye> withPage(int page) {
            return new State(items, page);
        }

        @Override
        public PaginatedState<VincentDye> withItems(List<VincentDye> items) {
            return new State(items, page);
        }
    }

    public static State initialState() {
        return new State(VincentDyes.all(), 0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Dye Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
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
    protected int[] getPaginatedSlots() {
        return DEFAULT_SLOTS;
    }

    @Override
    protected ItemStack.Builder renderItem(VincentDye dye, int index, HypixelPlayer player) {
        int year = SkyBlockCalendar.getYear();
        List<VincentDye> chosen = VincentDyes.masterpiece(year);
        int pick = chosen.indexOf(dye);
        Text boost = pick < 0 ? null : Text.of("<e>This dye is <a>{}x<e> as common during\n<d>SkyBlock Year <5>{}<e>!",
                VincentDyes.MASTERPIECE_MULTIPLIERS[pick], year);
        return dye.display(boost);
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, VincentDye item, int index) {
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, VincentDye item) {
        return false;
    }
}
