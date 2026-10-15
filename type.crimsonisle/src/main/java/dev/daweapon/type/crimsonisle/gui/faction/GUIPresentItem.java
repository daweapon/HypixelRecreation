package dev.daweapon.type.crimsonisle.gui.faction;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * The Dean's and Queen Nyx's "Present Item" menu: clicking the wanted item in the player's own
 * inventory presents it; anything else gets "They are not interested in this item!".
 */
public class GUIPresentItem extends StatelessView {
    private final ItemType wanted;
    private final Runnable onPresented;

    public GUIPresentItem(ItemType wanted, Runnable onPresented) {
        this.wanted = wanted;
        this.onPresented = onPresented;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Present Item", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(22, ItemStacks.item(Material.YELLOW_STAINED_GLASS_PANE, """
                <e>Present Item
                <7>Click an item in your inventory to
                <7>present it!"""));
    }

    @Override
    public boolean onBottomClick(ClickContext<DefaultState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) click.player();
        ItemStack stack = player.getInventory().getItemStack(click.slot());
        if (stack.isAir()) return false;
        if (!SkyBlockItem.isSkyBlockItem(stack) || new SkyBlockItem(stack).getAttributeHandler().getPotentialType() != wanted) {
            player.sendMessage("<c>They are not interested in this item!");
            return false;
        }
        player.closeInventory();
        onPresented.run();
        return false;
    }
}
