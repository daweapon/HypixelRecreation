package net.swofty.type.skyblockgeneric.gui.inventories.forge;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.forge.ForgeRecipe;
import net.swofty.type.skyblockgeneric.forge.ForgeService;
import net.swofty.type.skyblockgeneric.forge.ForgeTooltips;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

public class GUIForgeConfirm extends StatelessView {
    private static final int FURNACE_SLOT = 13;
    private static final int OUTPUT_SLOT = 16;
    private static final int CONFIRM_SLOT = 31;
    private static final int[] INGREDIENT_SLOTS = {10, 11, 19, 20, 28, 29, 37, 38};
    private final ForgeRecipe recipe;
    private final int forgeSlot;

    public GUIForgeConfirm(ForgeRecipe recipe, int forgeSlot) {
        this.recipe = recipe;
        this.forgeSlot = forgeSlot;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Confirm Process", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        int index = 0;
        for (Map.Entry<ItemType, Integer> input : recipe.inputTypes().entrySet()) {
            if (index >= INGREDIENT_SLOTS.length) break;
            layout.slot(INGREDIENT_SLOTS[index++], ingredient(input.getKey(), input.getValue()));
        }
        layout.slot(FURNACE_SLOT, ItemStacks.item(Material.FURNACE, "<a>Slot #" + forgeSlot
                + "\n<7>Duration: <b>" + ForgeTooltips.duration(ForgeService.durationMillis(player, recipe))));
        layout.slot(OUTPUT_SLOT, (_, _) -> ForgeTooltips.recipe(player, recipe, null));
        layout.slot(CONFIRM_SLOT, (_, _) -> ForgeService.canAfford(player, recipe)
                        ? ItemStacks.item(Material.GREEN_TERRACOTTA, "<a>Confirm\n<7>Start forging " + ForgeTooltips.name(recipe) + "<7>.\n<8>Forging can't be cancelled.")
                        : ItemStacks.item(Material.RED_TERRACOTTA, "<c>Missing Items\n<7>You don't have everything needed.\n\n<7>Items Required:\n" + ForgeTooltips.ingredients(recipe)),
                (_, c) -> {
                    String error = ForgeService.start(player, recipe, forgeSlot);
                    player.sendMessage(error != null ? error : "<a>You started forging " + ForgeTooltips.name(recipe) + "<a>!");
                    if (error != null) return;
                    c.pop();
                    c.pop();
                    c.pop();
                });
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    private static ItemStack.Builder ingredient(ItemType type, int amount) {
        return new NonPlayerItemUpdater(new SkyBlockItem(type, Math.min(amount, 99))).getUpdatedItem();
    }
}
