package dev.daweapon.type.hub.gui.vincent;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.PaginatedView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class GUIDyeGuide extends PaginatedView<VincentDye, GUIDyeGuide.State> {

    private static final List<String> SORTS = List.of("A to Z", "By Color", "Most Dropped", "Least Dropped", "Your Drops");

    public record State(List<VincentDye> items, int page, int sort, String query) implements PaginatedState<VincentDye> {
        @Override
        public PaginatedState<VincentDye> withPage(int page) {
            return new State(items, page, sort, query);
        }

        @Override
        public PaginatedState<VincentDye> withItems(List<VincentDye> items) {
            return new State(items, page, sort, query);
        }

        State withSort(int sort) {
            return new State(sorted(VincentDyes.all(), sort), 0, sort, query);
        }

        State withQuery(String query) {
            return new State(items, 0, sort, query);
        }
    }

    public static State initialState() {
        return new State(sorted(VincentDyes.all(), 0), 0, 0, "");
    }

    private static List<VincentDye> sorted(List<VincentDye> dyes, int sort) {
        Comparator<VincentDye> byName = Comparator.comparing(dye -> dye.name().toLowerCase(Locale.ROOT));
        Comparator<VincentDye> order = switch (sort) {
            case 1 -> Comparator.comparing((VincentDye dye) -> dye.hex() == null ? "~" : dye.hex()).thenComparing(byName);
            case 2 -> Comparator.comparing((VincentDye dye) -> !dye.droppable()).thenComparing(byName);
            case 3 -> Comparator.comparing(VincentDye::droppable).thenComparing(byName);
            default -> byName;
        };
        return dyes.stream().sorted(order).toList();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Dye Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        layout.slot(50, (s, _) -> sortButton(s.sort()), (_, c) -> c.session(Object.class).updateUnchecked(s -> {
            State current = (State) s;
            return current.withSort((current.sort() + 1) % SORTS.size());
        }));
        layout.slot(51, ItemStacks.item(Material.OAK_SIGN, "<a>Search\n<7>Find dyes by name!\n\n<e>Click to search!"), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            State current = state;
            new HypixelSignGUI(player).open(new String[]{"Enter your query", "to find dyes"})
                    .thenAccept(input -> MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                        if (!player.isOnline() || input == null) return;
                        player.openView(new GUIDyeGuide(), current.withQuery(input.trim().toLowerCase(Locale.ROOT)));
                    }));
        });
    }

    private static ItemStack.Builder sortButton(int selected) {
        StringBuilder text = new StringBuilder("<a>Sort\n");
        for (int i = 0; i < SORTS.size(); i++) {
            text.append(i == selected ? "<b>▶ " : "<7> ").append(SORTS.get(i)).append('\n');
        }
        return ItemStacks.item(Material.HOPPER, text.append("\n<e>Click to switch sort!").toString());
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
        return !state.query().isEmpty() && !item.name().toLowerCase(Locale.ROOT).contains(state.query());
    }
}
