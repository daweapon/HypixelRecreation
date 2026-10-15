package dev.daweapon.type.skyblockgeneric.gui.inventories.forge;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.forge.ForgeCategory;
import dev.daweapon.type.skyblockgeneric.forge.ForgeRecipe;
import dev.daweapon.type.skyblockgeneric.forge.ForgeRegistry;
import dev.daweapon.type.skyblockgeneric.forge.ForgeService;
import dev.daweapon.type.skyblockgeneric.forge.ForgeTooltips;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIForgeRecipes extends StatelessView {
    private static final int[] RECIPE_SLOTS = recipeSlots();
    private final ForgeCategory category;
    private final int page;
    private final int forgeSlot;

    public GUIForgeRecipes(ForgeCategory category, int page, int forgeSlot) {
        this.category = category;
        this.page = page;
        this.forgeSlot = forgeSlot;
    }

    private static int[] recipeSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row = 1; row <= 4; row++) for (int col = 1; col <= 7; col++) slots.add(row * 9 + col);
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private int pages() {
        return Math.max(1, (ForgeRegistry.get().inCategory(category).size() + RECIPE_SLOTS.length - 1) / RECIPE_SLOTS.length);
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        int pages = pages();
        String title = pages > 1 ? "(" + (Math.min(page, pages - 1) + 1) + "/" + pages + ") " + category.displayName() : category.displayName();
        return new ViewConfiguration<>(title, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        List<ForgeRecipe> recipes = ForgeRegistry.get().inCategory(category);
        int pages = pages();
        int current = Math.min(page, pages - 1);

        for (int index = 0; index < RECIPE_SLOTS.length; index++) {
            int at = current * RECIPE_SLOTS.length + index;
            if (at >= recipes.size()) break;
            ForgeRecipe recipe = recipes.get(at);
            layout.slot(RECIPE_SLOTS[index], (_, _) -> ForgeTooltips.recipe(player, recipe,
                            ForgeService.reasonUnavailable(player, recipe) == null ? "<e>Click to select!" : null),
                    (_, c) -> {
                        String blocked = ForgeService.reasonUnavailable(player, recipe);
                        if (blocked != null) {
                            player.sendMessage(blocked);
                            return;
                        }
                        c.push(new GUIForgeConfirm(recipe, forgeSlot));
                    });
        }

        if (current > 0) {
            layout.slot(45, ItemStacks.item(Material.ARROW, "<a>Previous Page"), (_, c) -> c.replace(new GUIForgeRecipes(category, current - 1, forgeSlot)));
        }
        if (current < pages - 1) {
            layout.slot(53, ItemStacks.item(Material.ARROW, "<a>Next Page"), (_, c) -> c.replace(new GUIForgeRecipes(category, current + 1, forgeSlot)));
        }
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }
}
