package net.swofty.type.skyblockgeneric.gui.inventories.enchantment;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// One enchantment in the Enchantments Guide: a book per level with its effect and Exp Level apply cost.
public class GUIEnchantmentLevels extends StatelessView {
    private static final int[] LEVEL_SLOTS = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private final EnchantmentType type;

    public GUIEnchantmentLevels(EnchantmentType type) {
        this.type = type;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(type.getName(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        layout.slot(4, (_, c) -> summary((SkyBlockPlayer) c.player()));
        SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
        int first = EnchantmentGuideLore.minimumLevel(type, viewer);
        int last = Math.min(EnchantmentGuideLore.maximumLevel(type, viewer), first + LEVEL_SLOTS.length - 1);
        for (int level = first; level <= last; level++) {
            int shown = level;
            layout.slot(LEVEL_SLOTS[level - first], (_, c) -> levelBook(shown, (SkyBlockPlayer) c.player()));
        }
    }

    private ItemStack.Builder summary(SkyBlockPlayer player) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.parseLenient("<8>" + (type.getEnch().isUltimate() ? "Ultimate Enchantment" : "Enchantment")));
        lore.add(Text.empty());
        lore.addAll(EnchantmentGuideLore.sources(type, player));
        lore.add(Text.empty());
        lore.addAll(EnchantmentGuideLore.appliedTo(type));
        List<Text> conflicts = EnchantmentGuideLore.conflicts(type);
        if (!conflicts.isEmpty()) {
            lore.add(Text.empty());
            lore.addAll(conflicts);
        }
        if (type.getEnch().isUltimate()) {
            lore.add(Text.empty());
            lore.add(Text.parseLenient("<c>You can only have 1 Ultimate"));
            lore.add(Text.parseLenient("<c>Enchantment on an item!"));
        }
        return ItemStacks.item(Material.BOOK, 1, Text.parseLenient(type.getEnch().isUltimate() ? "<d><l>" + type.getName() : "<9>" + type.getName()), lore);
    }

    private ItemStack.Builder levelBook(int level, SkyBlockPlayer player) {
        List<Text> lore = new ArrayList<>(EnchantmentGuideLore.description(type, level, player));
        if (level <= type.getEnch().getLevelsToApply(player).maximumLevel()) {
            lore.add(Text.empty());
            lore.add(Text.of("<7>Apply Cost: <3>{} Exp Levels", type.getApplyCost(level, player)));
        } else {
            lore.add(Text.empty());
            lore.add(Text.of("<8>Reached by combining or upgrading."));
        }
        return ItemStacks.item(Material.ENCHANTED_BOOK, Math.min(level, 64), Text.parseLenient(EnchantmentGuideLore.title(type, level)), lore);
    }
}
