package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulPaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseConfig;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseEffect;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import net.swofty.type.skyblockgeneric.garden.greenhouse.MutationCatalogue;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIAllMutations extends StatefulPaginatedView<MutationCatalogue.Mutation, GUIAllMutations.State> {

    @Override
    public State initialState() {
        return new State(MutationCatalogue.get().all(), 0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, _) -> "All Mutations", InventoryType.CHEST_6_ROW);
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
    protected ItemStack.Builder renderItem(MutationCatalogue.Mutation mutation, int index, HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        boolean analyzed = GreenhouseService.data(player).getAnalyzed().contains(mutation.id());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>Mutation Crop"));
        lore.add(Text.of("<7>Size: <a>{}", mutation.size()));
        lore.add(Text.of("<7>Growth Surface: <a>{}", mutation.surface()));
        lore.add(Text.of("<7>Growth Stages: <a>{}", mutation.stages()));
        lore.add(Text.empty());
        if (!mutation.effects().isEmpty()) {
            lore.add(Text.of("<7>Effects:"));
            for (GreenhouseEffect effect : mutation.effects()) {
                lore.add(Text.of("<8>> {}{}", effect.isNegative() ? "<c>" : "<a>", effect.getDisplayName()));
            }
            lore.add(Text.empty());
        }
        lore.add(Text.of("<7>Spreading Conditions"));
        if (!analyzed) {
            lore.add(Text.of("<c>UNKNOWN"));
            lore.add(Text.of("<8>Analyze this mutation to learn them."));
        } else if (mutation.conditions().isEmpty()) {
            lore.add(Text.of("<a>{}", mutation.note() == null ? "Special" : mutation.note()));
        } else {
            for (MutationCatalogue.Condition condition : mutation.conditions()) {
                lore.add(Text.of("<a>{}x {}", condition.count(), StringUtility.toNormalCase(condition.crop())));
            }
        }
        if (analyzed && mutation.behaviour() != null) {
            lore.add(Text.empty());
            lore.addAll(Text.of("<7><wrap:30>{}</wrap>", mutation.behaviour()).lines());
        }
        Material icon = analyzed ? Material.PLAYER_HEAD : Material.GRAY_DYE;
        net.swofty.type.skyblockgeneric.item.SkyBlockItem head = mutation.item() == null ? null : new SkyBlockItem(mutation.item());
        if (analyzed && head != null) {
            return ItemStacks.item(icon, Text.of(rarityColour(mutation.rarity()) + mutation.name()), lore);
        }
        return ItemStacks.item(icon, Text.of("<7>" + (analyzed ? mutation.name() : "???")), lore);
    }

    private static String rarityColour(String rarity) {
        return switch (rarity) {
            case "UNCOMMON" -> "<a>";
            case "RARE" -> "<9>";
            case "EPIC" -> "<5>";
            case "LEGENDARY" -> "<6>";
            default -> "<f>";
        };
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, MutationCatalogue.Mutation mutation, int index) {
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, MutationCatalogue.Mutation mutation) {
        return false;
    }

    public record State(List<MutationCatalogue.Mutation> items, int page) implements PaginatedState<MutationCatalogue.Mutation> {
        @Override
        public PaginatedState<MutationCatalogue.Mutation> withPage(int page) {
            return new State(items, page);
        }

        @Override
        public PaginatedState<MutationCatalogue.Mutation> withItems(List<MutationCatalogue.Mutation> items) {
            return new State(items, page);
        }
    }
}
