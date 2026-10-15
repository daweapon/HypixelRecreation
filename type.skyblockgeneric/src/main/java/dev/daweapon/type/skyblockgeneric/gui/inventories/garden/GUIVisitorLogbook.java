package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulPaginatedView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.garden.GardenProgress;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.npc.GardenNpcSkins;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GUIVisitorLogbook extends StatefulPaginatedView<VisitorCatalogue.Visitor, GUIVisitorLogbook.State> {

    public enum Sort {
        ALPHABETICAL("Alphabetical"), RARITY("Rarity"), GARDEN_LEVEL("Garden Level"), TIMES_VISITED("Times Visited"), OFFERS_ACCEPTED("Offers Accepted");

        private final String label;

        Sort(String label) {
            this.label = label;
        }

        Sort next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public enum Filter {
        ALL("All"), VISITED("Visited"), NOT_VISITED("Not Visited"), UNCOMMON("Uncommon"), RARE("Rare"), LEGENDARY("Legendary"), MYTHIC("Mythic"), SPECIAL("Special");

        private final String label;

        Filter(String label) {
            this.label = label;
        }

        Filter next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    @Override
    public State initialState() {
        return new State(VisitorCatalogue.get().visitors().stream()
                .sorted(Comparator.comparing(VisitorCatalogue.Visitor::name)).toList(), 0, Sort.ALPHABETICAL, Filter.ALL);
    }

    private static List<VisitorCatalogue.Visitor> build(SkyBlockPlayer player, Sort sort, Filter filter) {
        VisitorData data = Gardens.profile(player).visitors();
        Comparator<VisitorCatalogue.Visitor> byName = Comparator.comparing(VisitorCatalogue.Visitor::name);
        Comparator<VisitorCatalogue.Visitor> order = switch (sort) {
            case ALPHABETICAL -> byName;
            case RARITY -> Comparator.<VisitorCatalogue.Visitor>comparingInt(v -> -v.rarity().ordinal()).thenComparing(byName);
            case GARDEN_LEVEL -> Comparator.<VisitorCatalogue.Visitor>comparingInt(VisitorCatalogue.Visitor::gardenLevel).thenComparing(byName);
            case TIMES_VISITED -> Comparator.<VisitorCatalogue.Visitor>comparingInt(v -> -data.timesVisited(v.id())).thenComparing(byName);
            case OFFERS_ACCEPTED -> Comparator.<VisitorCatalogue.Visitor>comparingInt(v -> -data.offersAccepted(v.id())).thenComparing(byName);
        };
        return VisitorCatalogue.get().visitors().stream().filter(v -> switch (filter) {
            case ALL -> true;
            case VISITED -> data.timesVisited(v.id()) > 0;
            case NOT_VISITED -> data.timesVisited(v.id()) == 0;
            default -> v.rarity().name().equals(filter.name());
        }).sorted(order).toList();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, _) -> "Visitor's Logbook", InventoryType.CHEST_6_ROW);
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
        Components.close(layout, 49);
        layout.slot(4, (_, c) -> summary((SkyBlockPlayer) c.player()));
        layout.slot(48, (_, _) -> option(Material.HOPPER, "<a>Sort", state.sort().label, java.util.Arrays.stream(Sort.values()).map(v -> v.label).toList()),
                (_, c) -> {
                    State current = state;
                    Sort next = current.sort().next();
                    c.session(State.class).setState(new State(build((SkyBlockPlayer) c.player(), next, current.filter()), 0, next, current.filter()));
                });
        layout.slot(50, (_, _) -> option(Material.ENDER_EYE, "<a>Filter", state.filter().label, java.util.Arrays.stream(Filter.values()).map(v -> v.label).toList()),
                (_, c) -> {
                    State current = state;
                    Filter next = current.filter().next();
                    c.session(State.class).setState(new State(build((SkyBlockPlayer) c.player(), current.sort(), next), 0, current.sort(), next));
                });
    }

    private static ItemStack.Builder option(Material material, String title, String current, List<String> options) {
        List<Text> lore = new ArrayList<>();
        for (String option : options) lore.add(Text.of(option.equals(current) ? "<a>▶ " + option : "<8>  " + option));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to change!"));
        return ItemStacks.item(material, Text.of(title), lore);
    }

    private ItemStack.Builder summary(SkyBlockPlayer player) {
        VisitorData data = Gardens.profile(player).visitors();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Keeps track of who has visited your"));
        lore.add(Text.of("<7>Garden, and the requirements for"));
        lore.add(Text.of("<7>visitors to show up."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Unique Visitors Served: <a>{}<7>/<a>{}", data.uniqueServed(), VisitorCatalogue.get().visitors().size()));
        lore.add(Text.of("<7>Offers Accepted: <a>{}", data.totalAccepted()));
        return ItemStacks.item(Material.BOOK, Text.of("<a>Visitor's Logbook"), lore);
    }

    @Override
    protected ItemStack.Builder renderItem(VisitorCatalogue.Visitor visitor, int index, HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        var profile = Gardens.profile(player);
        VisitorData data = profile.visitors();
        int visited = data.timesVisited(visitor.id());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(visitor.rarity().getColour() + "<l>" + visitor.rarity().getLabel()));
        lore.add(Text.empty());
        if (visited > 0) {
            lore.add(Text.of("<7>Times Visited: <a>{}", visited));
            lore.add(Text.of("<7>Offers Accepted: <a>{}", data.offersAccepted(visitor.id())));
        } else {
            lore.add(Text.of("<7>Requirements:"));
            boolean level = GardenProgress.level(profile) >= visitor.gardenLevel();
            lore.add(Text.of((level ? "<a>✔" : "<c>✘") + " <7>Garden Level {}", visitor.gardenLevel()));
            if (visitor.requirement().text() != null) {
                lore.add(Text.of("<8>• <7>{}", visitor.requirement().text()));
            }
            if (!visitor.available()) {
                lore.add(Text.empty());
                lore.add(Text.of("<c>Not added yet!"));
            }
        }
        if (visited == 0) {
            return ItemStacks.item(Material.GRAY_DYE, Text.of("<7>" + visitor.name()), lore);
        }
        PlayerSkin skin = GardenNpcSkins.playerSkin(visitor.id());
        return skin == null
                ? ItemStacks.item(Material.PLAYER_HEAD, Text.of(visitor.coloured()), lore)
                : ItemStacks.head(skin, Text.of(visitor.coloured()), lore);
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, VisitorCatalogue.Visitor visitor, int index) {
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, VisitorCatalogue.Visitor visitor) {
        return false;
    }

    public record State(List<VisitorCatalogue.Visitor> items, int page, Sort sort, Filter filter) implements PaginatedState<VisitorCatalogue.Visitor> {
        @Override
        public PaginatedState<VisitorCatalogue.Visitor> withPage(int page) {
            return new State(items, page, sort, filter);
        }

        @Override
        public PaginatedState<VisitorCatalogue.Visitor> withItems(List<VisitorCatalogue.Visitor> items) {
            return new State(items, page, sort, filter);
        }
    }
}
