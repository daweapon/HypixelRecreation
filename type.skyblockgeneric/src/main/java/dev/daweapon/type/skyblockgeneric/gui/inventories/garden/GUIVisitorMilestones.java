package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulPaginatedView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIVisitorMilestones extends StatefulPaginatedView<VisitorCatalogue.MilestoneTier, GUIVisitorMilestones.State> {
    private final boolean unique;

    public GUIVisitorMilestones(boolean unique) {
        this.unique = unique;
    }

    @Override
    public State initialState() {
        VisitorCatalogue catalogue = VisitorCatalogue.get();
        return new State(unique ? catalogue.uniqueMilestones() : catalogue.acceptedMilestones(), 0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, _) -> unique ? "Unique Visitors Served" : "Offers Accepted", InventoryType.CHEST_6_ROW);
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
        layout.slot(48, (_, _) -> ItemStacks.item(Material.HOPPER, Text.of(unique ? "<a>Offers Accepted" : "<a>Unique Visitors Served"),
                        List.of(Text.of("<e>Click to switch track!"))),
                (_, c) -> c.push(new GUIVisitorMilestones(!unique)));
        layout.slot(4, (_, c) -> {
            VisitorData data = Gardens.profile((SkyBlockPlayer) c.player()).visitors();
            return ItemStacks.item(Material.EMERALD, Text.of(unique ? "<a>Unique Visitors Served" : "<a>Offers Accepted"), List.of(
                    Text.of("<7>Progress: <a>{}", unique ? data.uniqueServed() : data.totalAccepted())));
        });
    }

    @Override
    protected ItemStack.Builder renderItem(VisitorCatalogue.MilestoneTier tier, int index, HypixelPlayer p) {
        VisitorData data = Gardens.profile((SkyBlockPlayer) p).visitors();
        int progress = unique ? data.uniqueServed() : data.totalAccepted();
        boolean done = progress >= tier.cumulative();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Requires <a>{} <7>{}", tier.cumulative(), unique ? "unique visitors served" : "offers accepted"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Rewards:"));
        lore.add(Text.of(" <8>+<a>{} Garden Experience", tier.gardenXp()));
        lore.add(Text.of(" <8>+<3>{} Farming Experience", StringUtility.commaify(tier.farmingXp())));
        lore.add(Text.of(" <8>+<b>{} SkyBlock XP", tier.skyblockXp()));
        lore.add(Text.empty());
        lore.add(Text.of(done ? "<a>✔ Reached!" : "<7>Progress: <a>" + Math.min(progress, tier.cumulative()) + "<7>/<a>" + tier.cumulative()));
        return ItemStacks.item(done ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                Text.of((done ? "<a>" : "<c>") + "Tier " + StringUtility.getAsRomanNumeral(tier.tier())), lore);
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, VisitorCatalogue.MilestoneTier tier, int index) {
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, VisitorCatalogue.MilestoneTier tier) {
        return false;
    }

    public record State(List<VisitorCatalogue.MilestoneTier> items, int page) implements PaginatedState<VisitorCatalogue.MilestoneTier> {
        @Override
        public PaginatedState<VisitorCatalogue.MilestoneTier> withPage(int page) {
            return new State(items, page);
        }

        @Override
        public PaginatedState<VisitorCatalogue.MilestoneTier> withItems(List<VisitorCatalogue.MilestoneTier> items) {
            return new State(items, page);
        }
    }
}
