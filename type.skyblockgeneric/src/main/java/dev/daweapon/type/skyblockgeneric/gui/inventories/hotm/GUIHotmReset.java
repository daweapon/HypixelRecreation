package dev.daweapon.type.skyblockgeneric.gui.inventories.hotm;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIHotmReset extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Reset Heart of the Mountain", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        layout.slot(11, ItemStacks.item(Material.GREEN_TERRACOTTA, """
                <a>Confirm
                <7>Reset every perk and refund the Powder
                <7>and Tokens you spent on them."""), (_, c) -> {
            HotmService.reset(player);
            player.sendMessage("<a>Your Heart of the Mountain perks were reset. Powder and Tokens were refunded.");
            c.pop();
        });
        layout.slot(15, ItemStacks.item(Material.RED_TERRACOTTA, "<c>Cancel"), (_, c) -> c.pop());
    }
}
