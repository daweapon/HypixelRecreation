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
import net.swofty.type.skyblockgeneric.forge.ForgeRecipe;
import net.swofty.type.skyblockgeneric.forge.ForgeService;
import net.swofty.type.skyblockgeneric.forge.ForgeTooltips;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIForgeConfirm extends StatelessView {
    private final ForgeRecipe recipe;

    public GUIForgeConfirm(ForgeRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Confirm Process", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        layout.slot(13, (_, _) -> ForgeTooltips.recipe(player, recipe, null));
        layout.slot(11, ItemStacks.item(Material.GREEN_TERRACOTTA, "<a>Confirm\n<7>Start forging " + ForgeTooltips.name(recipe)
                + "<7>.\n<8>Forging can't be cancelled."), (_, c) -> {
            String error = ForgeService.start(player, recipe);
            player.sendMessage(error != null ? error : "<a>You started forging " + ForgeTooltips.name(recipe) + "<a>!");
            c.pop();
        });
        layout.slot(15, ItemStacks.item(Material.RED_TERRACOTTA, "<c>Cancel"), (_, c) -> c.pop());
    }
}
