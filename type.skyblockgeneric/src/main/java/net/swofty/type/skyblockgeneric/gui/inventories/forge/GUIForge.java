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
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM.ForgeProcess;
import net.swofty.type.skyblockgeneric.forge.ForgeCategory;
import net.swofty.type.skyblockgeneric.forge.ForgeRecipe;
import net.swofty.type.skyblockgeneric.forge.ForgeRegistry;
import net.swofty.type.skyblockgeneric.forge.ForgeService;
import net.swofty.type.skyblockgeneric.forge.ForgeTooltips;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIForge extends StatelessView {
    private static final int[] FURNACE_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] CATEGORY_SLOTS = {28, 29, 30, 31, 32, 33, 34, 39, 41};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("The Forge", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        for (int index = 0; index < FURNACE_SLOTS.length; index++) {
            int forgeSlot = index + 1;
            layout.slot(FURNACE_SLOTS[index], (_, _) -> furnace(player, forgeSlot), (_, c) -> {
                ForgeProcess process = ForgeService.processIn(player, forgeSlot);
                if (process == null || !ForgeService.isFinished(process)) return;
                ForgeRecipe recipe = ForgeRegistry.get().recipe(process.recipeId());
                String error = ForgeService.claim(player, forgeSlot);
                player.sendMessage(error != null ? error : "<a>You collected " + ForgeTooltips.name(recipe) + "<a> from the Forge!");
                c.session(Object.class).refresh();
            });
        }

        layout.slot(22, ItemStacks.item(Material.GRAY_STAINED_GLASS_PANE, "<a>Select Process\n<7>Pick a category below, then the item to forge."));
        ForgeCategory[] categories = ForgeCategory.values();
        for (int index = 0; index < categories.length; index++) {
            ForgeCategory category = categories[index];
            int total = ForgeRegistry.get().inCategory(category).size();
            long available = ForgeRegistry.get().inCategory(category).stream().filter(ForgeRecipe::available).count();
            layout.slot(CATEGORY_SLOTS[index], ItemStacks.item(category.icon(), "<a>" + category.displayName()
                    + "\n<7>" + available + "<8>/" + total + " <7>recipes available\n\n<e>Click to view!"),
                    (_, c) -> c.push(new GUIForgeRecipes(category, 0)));
        }

        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
    }

    private static net.minestom.server.item.ItemStack.Builder furnace(SkyBlockPlayer player, int forgeSlot) {
        if (forgeSlot > ForgeService.slots(player)) {
            int tier = Math.max(2, forgeSlot);
            return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, "<c>Locked Slot #" + forgeSlot
                    + "\n<7>Requires <5>Heart of the Mountain Tier " + tier + "<7>.");
        }
        ForgeProcess process = ForgeService.processIn(player, forgeSlot);
        if (process == null) {
            return ItemStacks.item(Material.FURNACE, "<a>Forge Slot #" + forgeSlot + "\n<7>Empty\n\n<e>Select a process below!");
        }
        ForgeRecipe recipe = ForgeRegistry.get().recipe(process.recipeId());
        if (ForgeService.isFinished(process)) {
            return ItemStacks.appendLore(ForgeTooltips.icon(recipe), "\n<7>Forge Slot #" + forgeSlot + "\n<a>Finished!\n\n<e>Click to collect!");
        }
        return ItemStacks.appendLore(ForgeTooltips.icon(recipe), "\n<7>Forge Slot #" + forgeSlot + "\n<7>Time remaining: <a>"
                + ForgeTooltips.duration(ForgeService.remainingMillis(process)) + "\n\n<8>Forging can't be cancelled.\n<e>Click to refresh!");
    }
}
