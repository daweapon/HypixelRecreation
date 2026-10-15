package dev.daweapon.type.skyblockgeneric.gui.inventories.museum;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.Layouts;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import dev.daweapon.type.skyblockgeneric.museum.MuseumCategory;
import dev.daweapon.type.skyblockgeneric.museum.MuseumDisplays;
import dev.daweapon.type.skyblockgeneric.museum.MuseumEntry;
import dev.daweapon.type.skyblockgeneric.museum.MuseumService;
import dev.daweapon.type.skyblockgeneric.museum.MuseumState;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class GUIMuseumCategory implements StatefulView<GUIMuseumCategory.State> {
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public enum Sort {
        ASCENDING_XP("Ascending XP"),
        DESCENDING_XP("Descending XP"),
        MISSING_FIRST("Missing First"),
        A_TO_Z("A to Z"),
        Z_TO_A("Z to A");

        private final String label;

        Sort(String label) {
            this.label = label;
        }

        Sort step(int direction) {
            Sort[] values = values();
            return values[Math.floorMod(ordinal() + direction, values.length)];
        }
    }

    public record State(@Nullable MuseumCategory category, String query, int page, Sort sort, boolean extraDetails) {
        public static State of(MuseumCategory category) {
            return new State(category, "", 0, Sort.ASCENDING_XP, true);
        }

        public static State search(String query) {
            return new State(null, query, 0, Sort.ASCENDING_XP, true);
        }

        State withPage(int page) {
            return new State(category, query, page, sort, extraDetails);
        }

        State withSort(Sort sort) {
            return new State(category, query, 0, sort, extraDetails);
        }

        State toggleDetails() {
            return new State(category, query, page, sort, !extraDetails);
        }
    }

    @Override
    public State initialState() {
        return State.of(MuseumCategory.COMBAT);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> state.category() == null
                ? "Museum Search"
                : "Museum ➜ " + state.category().getDisplayName(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(Layouts.border(0, 53));
        DatapointMuseum.MuseumData data = ((SkyBlockPlayer) ctx.player()).getMuseumData();
        List<MuseumEntry> entries = entries(state, data);
        int pages = Math.max(1, (int) Math.ceil(entries.size() / (double) SLOTS.length));
        int page = Math.min(state.page(), pages - 1);

        for (int index = 0; index < SLOTS.length; index++) {
            int entryIndex = page * SLOTS.length + index;
            if (entryIndex >= entries.size()) {
                layout.slot(SLOTS[index], (s, c) -> ItemStack.AIR.builder());
                continue;
            }
            MuseumEntry entry = entries.get(entryIndex);
            layout.slot(SLOTS[index], (s, c) -> MuseumEntryIcon.render(
                    ((SkyBlockPlayer) c.player()).getMuseumData(), entry, s.extraDetails()),
                    (click, c) -> onEntryClick(c, entry));
        }

        if (state.category() != null) {
            layout.slot(4, (s, c) -> header(((SkyBlockPlayer) c.player()).getMuseumData(), s.category()));
        }
        if (page > 0) {
            layout.slot(45, (s, c) -> ItemStacks.item(Material.ARROW, "<a>Previous Page\n<e>Page {}", page),
                    (click, c) -> c.session(State.class).update(s -> s.withPage(page - 1)));
        }
        if (page < pages - 1) {
            layout.slot(53, (s, c) -> ItemStacks.item(Material.ARROW, "<a>Next Page\n<e>Page {}", page + 2),
                    (click, c) -> c.session(State.class).update(s -> s.withPage(page + 1)));
        }

        layout.slot(47, (s, c) -> detailsToggle(s.extraDetails()),
                (click, c) -> c.session(State.class).update(State::toggleDetails));
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        layout.slot(50, (s, c) -> GUIYourMuseum.searchButton(), (click, c) -> GUIYourMuseum.openSearch(c));
        layout.slot(51, (s, c) -> sortButton(s.sort()), (click, c) -> {
            int direction = click.click() instanceof Click.Right ? -1 : 1;
            c.session(State.class).update(s -> s.withSort(s.sort().step(direction)));
        });
    }

    @Override
    public boolean onBottomClick(ClickContext<State> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        MuseumService.donate(player, click.slot());
        MuseumDisplays.updateDisplay(player);
        ctx.session(State.class).refresh();
        return false;
    }

    private void onEntryClick(ViewContext ctx, MuseumEntry entry) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        MuseumState state = MuseumService.stateOf(player.getMuseumData(), entry);
        if (state == MuseumState.UNAVAILABLE) {
            player.sendMessage(MuseumEntry.UNAVAILABLE_REASON);
            return;
        }
        if (state != MuseumState.IN_MUSEUM) return;
        if (MuseumService.retrieve(player, entry)) {
            MuseumDisplays.updateDisplay(player);
            ctx.session(State.class).refresh();
        }
    }

    private static List<MuseumEntry> entries(State state, DatapointMuseum.MuseumData data) {
        List<MuseumEntry> entries = new ArrayList<>(state.category() == null
                ? MuseumService.catalogue().all()
                : MuseumService.catalogue().entries(state.category()));
        if (!state.query().isEmpty()) {
            String query = state.query().toLowerCase(Locale.ROOT);
            entries.removeIf(entry -> !entry.name().toLowerCase(Locale.ROOT).contains(query));
        }
        Comparator<MuseumEntry> byXp = Comparator.comparingInt(MuseumEntry::xp).thenComparing(MuseumEntry::name);
        Comparator<MuseumEntry> byName = Comparator.comparing(MuseumEntry::name);
        entries.sort(switch (state.sort()) {
            case ASCENDING_XP -> byXp;
            case DESCENDING_XP -> byXp.reversed();
            case MISSING_FIRST -> Comparator.comparing((MuseumEntry entry) -> data.isCredited(entry.id())).thenComparing(byXp);
            case A_TO_Z -> byName;
            case Z_TO_A -> byName.reversed();
        });
        return entries;
    }

    private static ItemStack.Builder header(DatapointMuseum.MuseumData data, MuseumCategory category) {
        long donated = MuseumService.donatedCount(data, category);
        List<Text> lore = new ArrayList<>();
        if (category.isSpecial()) {
            lore.add(Text.of("<7>View all of the <d>Special Items <7>that you"));
            lore.add(Text.of("<7>have donated to the <9>Museum<7>!"));
            lore.add(Text.empty());
            lore.add(Text.of("<7>These items don't count towards"));
            lore.add(Text.of("<7>Museum progress and rewards."));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Items Donated: <b>{}<9>/<b>{}", donated, MuseumCategory.SPECIAL_ITEM_LIMIT));
        } else {
            long total = MuseumService.catalogue().entries(category).size();
            lore.add(Text.of("<7>View all of the items related to the"));
            lore.add(Text.of(category.getColor() + "{} Skill <7>that you have donated", category.getDisplayName()));
            lore.add(Text.of("<7>to the <9>Museum<7>!"));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Items Donated: <e>{}<6>%", GUIYourMuseum.percent(donated, total)));
            lore.add(GUIYourMuseum.progressBar(donated, total));
        }
        return ItemStacks.item(category.getIcon(), 1, Text.of(category.coloredName()), lore);
    }

    private static ItemStack.Builder detailsToggle(boolean enabled) {
        return ItemStacks.item(enabled ? Material.LIME_TERRACOTTA : Material.RED_TERRACOTTA, 1,
                Text.of("<a>Extra Details"), List.of(
                        Text.of("<7>Show info on your donated items:"),
                        Text.of("<8>- <7>Date of the donation"),
                        Text.of("<8>- <7>Item Value"),
                        Text.of("<8>- <7>Clean Item Value"),
                        Text.of("<8>- <7>Museum Display Slot"),
                        Text.empty(),
                        Text.of(enabled ? "<7>Toggled: <a><l>ON" : "<7>Toggled: <c><l>OFF"),
                        Text.empty(),
                        Text.of("<e>Click to toggle!")));
    }

    private static ItemStack.Builder sortButton(Sort current) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        for (Sort sort : Sort.values()) {
            lore.add(sort == current ? Text.of("<b>▶ {}", sort.label) : Text.of("<7>   {}", sort.label));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<b>Right-click to go backwards!"));
        lore.add(Text.of("<e>Click to switch sort!"));
        return ItemStacks.item(Material.HOPPER, 1, Text.of("<a>Sort"), lore);
    }
}
