package net.swofty.type.skyblockgeneric.gui.inventories.forge;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.forge.ForgeCategory;

public class GUIForgeProcess extends StatelessView {
    private static final int[] CATEGORY_SLOTS = {11, 13, 15, 20, 22, 24, 29, 31, 33};
    private final int forgeSlot;

    public GUIForgeProcess(int forgeSlot) {
        this.forgeSlot = forgeSlot;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Select Process (Slot #" + forgeSlot + ")", InventoryType.CHEST_6_ROW);
    }

    private static Material icon(ForgeCategory category) {
        return switch (category) {
            case REFINING -> Material.IRON_INGOT;
            case FORGING -> Material.IRON_BLOCK;
            case TOOLS -> Material.PRISMARINE_SHARD;
            case GEAR -> Material.GOLDEN_CHESTPLATE;
            case REFORGE_STONES -> Material.ANVIL;
            case DRILL_PARTS -> Material.BLAZE_ROD;
            case PERFECT_GEMSTONES -> Material.EMERALD;
            case PETS -> Material.BONE;
            case OTHER -> Material.NETHER_STAR;
        };
    }

    private static String description(ForgeCategory category) {
        return switch (category) {
            case REFINING -> "Refine your ores into their pure forms.";
            case FORGING -> "Refine your ores into parts used in lots of gear.";
            case TOOLS -> "Forge drills and other tools for mining.";
            case GEAR -> "Forge armor and accessories for mining.";
            case REFORGE_STONES -> "Forge stones that apply new reforges.";
            case DRILL_PARTS -> "Forge your ore into improvements for your Drill.";
            case PERFECT_GEMSTONES -> "Forge flawless gemstones into perfect gemstones.";
            case PETS -> "Forge fossils into pets.";
            case OTHER -> "Forge keys, scrolls, beacons and more.";
        };
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        ForgeCategory[] categories = ForgeCategory.values();
        for (int index = 0; index < categories.length; index++) {
            ForgeCategory category = categories[index];
            layout.slot(CATEGORY_SLOTS[index], ItemStacks.item(icon(category),
                    "<a>" + category.displayName() + "\n<7>" + description(category) + "\n\n<e>Click to view!"),
                    (_, c) -> c.push(new GUIForgeRecipes(category, 0, forgeSlot)));
        }
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        layout.slot(50, ItemStacks.item(Material.OAK_SIGN, "<a>Forge Slot #" + forgeSlot + "\n<7>Pick a category, then the item to forge."));
    }
}
