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
import net.swofty.type.skyblockgeneric.forge.ForgeRecipe;
import net.swofty.type.skyblockgeneric.forge.ForgeRegistry;
import net.swofty.type.skyblockgeneric.forge.ForgeService;
import net.swofty.type.skyblockgeneric.forge.ForgeTooltips;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIForgeRecipes extends StatelessView {
    private static final int[] RECIPE_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private final ForgeCategory category;
    private final int page;

    public GUIForgeRecipes(ForgeCategory category, int page) {
        this.category = category;
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(category.displayName(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        List<ForgeRecipe> recipes = ForgeRegistry.get().inCategory(category);
        int pages = Math.max(1, (recipes.size() + RECIPE_SLOTS.length - 1) / RECIPE_SLOTS.length);
        int current = Math.min(page, pages - 1);

        for (int index = 0; index < RECIPE_SLOTS.length; index++) {
            int at = current * RECIPE_SLOTS.length + index;
            if (at >= recipes.size()) break;
            ForgeRecipe recipe = recipes.get(at);
            layout.slot(RECIPE_SLOTS[index], (_, _) -> ForgeTooltips.recipe(player, recipe, footer(player, recipe)),
                    (_, c) -> {
                        String blocked = ForgeService.reasonUnavailable(player, recipe);
                        if (blocked != null) {
                            player.sendMessage(blocked);
                            return;
                        }
                        c.push(new GUIForgeConfirm(recipe));
                    });
        }

        if (current > 0) {
            layout.slot(46, ItemStacks.item(Material.ARROW, "<a>Previous Page"), (_, c) -> c.replace(new GUIForgeRecipes(category, current - 1)));
        }
        if (current < pages - 1) {
            layout.slot(52, ItemStacks.item(Material.ARROW, "<a>Next Page"), (_, c) -> c.replace(new GUIForgeRecipes(category, current + 1)));
        }
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
    }

    private static String footer(SkyBlockPlayer player, ForgeRecipe recipe) {
        return ForgeService.reasonUnavailable(player, recipe) == null ? "<e>Click to forge!" : null;
    }
}
