package net.swofty.type.skyblockgeneric.gui.inventories.enchantment;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.PaginatedView;
import net.swofty.type.generic.gui.v2.StatefulPaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

// The Enchantments Guide: every enchantment with its levels, effects, sources, applicable items and conflicts.
public class GUIEnchantmentGuide extends StatefulPaginatedView<EnchantmentType, GUIEnchantmentGuide.State> {
    private static final Comparator<EnchantmentType> ORDER = Comparator
            .comparing((EnchantmentType type) -> type.getEnch().isUltimate())
            .thenComparing(EnchantmentType::getName, String.CASE_INSENSITIVE_ORDER);

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Enchantments Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    public State initialState() {
        return new State(Arrays.stream(EnchantmentType.values()).sorted(ORDER).toList(), 0, EnchantmentGuideFilter.ALL);
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
    protected boolean shouldFilterFromSearch(State state, EnchantmentType item) {
        return !state.filter().matches(item);
    }

    @Override
    protected ItemStack.Builder renderItem(EnchantmentType type, int index, HypixelPlayer viewer) {
        SkyBlockPlayer player = (SkyBlockPlayer) viewer;
        List<Text> lore = new ArrayList<>();
        lore.add(Text.parseLenient("<8>" + (type.getEnch().isUltimate() ? "Ultimate Enchantment" : "Enchantment")));
        lore.add(Text.empty());
        int max = EnchantmentGuideLore.maximumLevel(type, player);
        lore.addAll(EnchantmentGuideLore.description(type, max, player));
        lore.add(Text.empty());
        lore.addAll(EnchantmentGuideLore.sources(type, player));
        lore.add(Text.empty());
        lore.addAll(EnchantmentGuideLore.appliedTo(type));
        List<Text> conflicts = EnchantmentGuideLore.conflicts(type);
        if (!conflicts.isEmpty()) {
            lore.add(Text.empty());
            lore.addAll(conflicts);
        }
        lore.add(Text.empty());
        lore.add(Text.of("<7>Levels: <a>{}", EnchantmentGuideLore.levelRange(type, player)));
        lore.add(Text.of("<e>Click to view every level!"));
        return ItemStacks.item(Material.ENCHANTED_BOOK, 1, Text.parseLenient(EnchantmentGuideLore.title(type, max)), lore);
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, EnchantmentType item, int index) {
        ctx.push(new GUIEnchantmentLevels(item));
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        layout.slot(50, (s, c) -> ItemStacks.item(s.filter().icon, 1, "<a>Filter: <e>{}\n<7>Showing <a>{}<7> enchantments.\n\n<e>Left-click for the next category!\n<e>Right-click for the previous!",
                s.filter().displayName, s.items().stream().filter(s.filter()::matches).count()), (click, c) -> {
            int step = click.click() instanceof Click.Right ? -1 : 1;
            c.session(State.class).update(current -> current.withFilter(current.filter().next(step)));
        });
    }

    record State(List<EnchantmentType> items, int page, EnchantmentGuideFilter filter) implements PaginatedView.PaginatedState<EnchantmentType> {
        @Override
        public State withPage(int page) {
            return new State(items, page, filter);
        }

        @Override
        public State withItems(List<EnchantmentType> items) {
            return new State(items, page, filter);
        }

        State withFilter(EnchantmentGuideFilter filter) {
            return new State(items, 0, filter);
        }
    }
}
