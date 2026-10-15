package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.recipe;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.crafting.ShapedRecipe;
import dev.daweapon.type.skyblockgeneric.item.crafting.ShapelessRecipe;
import dev.daweapon.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// GUI listing crafting recipes tied to each slayer boss type
public class GUIRecipeSlayers extends StatelessView {

    private static final int[] CATEGORY_SLOTS = {20, 21, 22, 23, 24};

    private static final List<SkyBlockRecipe.RecipeType> SLAYER_TYPES = List.of(
            SkyBlockRecipe.RecipeType.REVENANT_HORROR,
            SkyBlockRecipe.RecipeType.TARANTULA_BROODFATHER,
            SkyBlockRecipe.RecipeType.SVEN_PACKMASTER,
            SkyBlockRecipe.RecipeType.VOIDGLOOM_SERAPH,
            SkyBlockRecipe.RecipeType.INFERNO_DEMONLORD
    );

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.recipe.slayers.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        ArrayList<SkyBlockRecipe> allRecipes = new ArrayList<>();
        allRecipes.addAll(ShapedRecipe.CACHED_RECIPES);
        allRecipes.addAll(ShapelessRecipe.CACHED_RECIPES);

        // Title item
        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();

            SkyBlockRecipe.RecipeType type = SkyBlockRecipe.RecipeType.SLAYER;

            ArrayList<SkyBlockRecipe> typeRecipes = new ArrayList<>();
            ArrayList<SkyBlockRecipe> allowedRecipes = new ArrayList<>();
            allRecipes.forEach(recipe -> {
                if (SLAYER_TYPES.contains(recipe.getRecipeType())) {
                    typeRecipes.add(recipe);
                }
            });

            typeRecipes.forEach(recipe -> {
                SkyBlockRecipe.CraftingResult result =
                        (SkyBlockRecipe.CraftingResult) recipe.getCanCraft().apply(player);

                if (result.allowed()) {
                    allowedRecipes.add(recipe);
                }
            });

            String unlockedPercentage = String.format("%.2f", (allowedRecipes.size() / (double) typeRecipes.size()) * 100);
            String categoryName = StringUtility.toNormalCase(type.name());

            return ItemStacks.item(type.getMaterial(), 1,
                    Text.key("gui_sbmenu.recipe.book.category", categoryName),
                    Text.keyLines("gui_sbmenu.recipe.book.category.lore", categoryName, unlockedPercentage,
                            GUIRecipeBook.progressBar(allowedRecipes.size(), typeRecipes.size())));
        });

        // Category items
        for (int i = 0; i < CATEGORY_SLOTS.length && i < SLAYER_TYPES.size(); i++) {
            SkyBlockRecipe.RecipeType type = SLAYER_TYPES.get(i);
            int slot = CATEGORY_SLOTS[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();

                ArrayList<SkyBlockRecipe> typeRecipes = new ArrayList<>();
                ArrayList<SkyBlockRecipe> allowedRecipes = new ArrayList<>();
                allRecipes.forEach(recipe -> {
                    if (recipe.getRecipeType() == type) {
                        typeRecipes.add(recipe);
                    }
                });

                typeRecipes.forEach(recipe -> {
                    SkyBlockRecipe.CraftingResult result =
                            (SkyBlockRecipe.CraftingResult) recipe.getCanCraft().apply(player);

                    if (result.allowed()) {
                        allowedRecipes.add(recipe);
                    }
                });

                String unlockedPercentage = String.format("%.2f", (allowedRecipes.size() / (double) typeRecipes.size()) * 100);
                String categoryName = StringUtility.toNormalCase(type.name());

                return ItemStacks.item(type.getMaterial(), 1,
                        Text.key("gui_sbmenu.recipe.book.category", categoryName),
                        Text.keyLines("gui_sbmenu.recipe.book.category.lore", categoryName, unlockedPercentage,
                                GUIRecipeBook.progressBar(allowedRecipes.size(), typeRecipes.size())));
            }, (_, c) -> c.push(new GUIRecipeCategory(type), GUIRecipeCategory.createInitialState((SkyBlockPlayer) c.player(), type)));
        }
    }
}
