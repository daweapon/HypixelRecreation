package dev.daweapon.type.theend.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.theend.altar.DraconicAltarRules;

import java.util.ArrayList;
import java.util.List;

public class GUIDraconicAltarGuide extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private static final double RARE_BELOW_PERCENT = 50;
    private static final double VERY_RARE_BELOW_PERCENT = 5;

    private final int page;

    public GUIDraconicAltarGuide(int page) {
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("(" + (page + 1) + "/" + pages() + ") Draconic Altar Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        layout.slot(4, ItemStacks.item(Material.CRYING_OBSIDIAN, """
                <c>Draconic Altar Guide
                <7>View a list of <d>End <7>and <5>Dragon <7>items
                <7>which can be sacrificed in the altar.

                <7>Sacrificing an item will give you
                <7><d>Dragon Essence <7>and a small chance
                <7>for <c>bonus items<7>."""), (_, c) -> c.push(new GUIDraconicAltar()));
        List<ItemType> items = items();
        for (int i = 0; i < SLOTS.length; i++) {
            int index = page * SLOTS.length + i;
            if (index >= items.size()) break;
            ItemType type = items.get(index);
            layout.slot(SLOTS[i], entry(type));
        }
        if (page + 1 < pages()) layout.slot(53, ItemStacks.item(Material.ARROW, "<a>Next Page\n<e>Page " + (page + 2)), (_, c) -> c.replace(new GUIDraconicAltarGuide(page + 1)));
        if (page > 0) layout.slot(45, ItemStacks.item(Material.ARROW, "<a>Previous Page\n<e>Page " + page), (_, c) -> c.replace(new GUIDraconicAltarGuide(page - 1)));
    }

    private static ItemStack.Builder entry(ItemType type) {
        var rules = DraconicAltarRules.of(type.name()).orElseThrow();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8><st>-----------------"));
        lore.add(Text.of("<7>Dragon Essence: <d>{}", rules.essence()));
        lore.add(Text.of("<7>Possible Bonus Items: "));
        for (DraconicAltarRules.Bonus bonus : rules.bonuses()) {
            ItemType reward = ItemType.get(bonus.itemId());
            if (reward == null) continue;
            String amount = bonus.min() == bonus.max() ? "" : bonus.min() + "-" + bonus.max() + "x ";
            lore.add(Text.of(" <8>{}{}{}", rarityTag(bonus.weight()), amount, new SkyBlockItem(reward).getDisplayNameText()));
        }
        return ItemStacks.appendLore(new SkyBlockItem(type).getItemStack().builder(), lore);
    }

    private static String rarityTag(double weight) {
        if (weight >= RARE_BELOW_PERCENT) return "";
        if (weight >= VERY_RARE_BELOW_PERCENT) return "[<9>Rare<8>] ";
        return "[<d>Very Rare<8>] ";
    }

    private static List<ItemType> items() {
        List<ItemType> items = new ArrayList<>();
        for (String id : DraconicAltarRules.itemIds()) {
            ItemType type = ItemType.get(id);
            if (type != null) items.add(type);
        }
        return items;
    }

    private static int pages() {
        return Math.max(1, (int) Math.ceil(items().size() / (double) SLOTS.length));
    }
}
