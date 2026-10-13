package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulPaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.GardenProgress;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIVisitorLogbook extends StatefulPaginatedView<VisitorCatalogue.Visitor, GUIVisitorLogbook.State> {

    @Override
    public State initialState() {
        return new State(VisitorCatalogue.get().visitors(), 0);
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
        Material icon = visited > 0 ? Material.PLAYER_HEAD : Material.GRAY_DYE;
        return ItemStacks.item(icon, Text.of(visited > 0 ? visitor.coloured() : "<7>" + visitor.name()), lore);
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, VisitorCatalogue.Visitor visitor, int index) {
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, VisitorCatalogue.Visitor visitor) {
        return false;
    }

    public record State(List<VisitorCatalogue.Visitor> items, int page) implements PaginatedState<VisitorCatalogue.Visitor> {
        @Override
        public PaginatedState<VisitorCatalogue.Visitor> withPage(int page) {
            return new State(items, page);
        }

        @Override
        public PaginatedState<VisitorCatalogue.Visitor> withItems(List<VisitorCatalogue.Visitor> items) {
            return new State(items, page);
        }
    }
}
