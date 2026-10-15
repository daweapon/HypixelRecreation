package dev.daweapon.type.skyblockgeneric.gui.inventories.essence;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Frozen Alex's menu, from the wiki's Frozen Alex/UI page. Salvaging has no system on this server
// yet, so its button only explains that.
public class GUIFrozenAlex extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Frozen Alex", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(20, ItemStacks.item(Material.BEACON, """
                <a>Salvage Items
                <7>Salvage weapons and armor for materials.

                <c>Salvaging isn't in this server yet."""));
        layout.slot(24, (_, c) -> shopButton((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUIIceEssenceShop()));
    }

    private static net.minestom.server.item.ItemStack.Builder shopButton(SkyBlockPlayer player) {
        return ItemStacks.head(EssenceGuideEntry.of(EssenceType.ICE).texture(), """
                <a>Ice Essence Shop
                <7>Exchange <d>Ice Essence <7>for
                <7>perks useful in the <c>Jerry's
                <c>Workshop <7>and beyond!

                <7>Your Ice Essence: <d>{}

                <e>Click to view!""", StringUtility.commaify(Essences.get(player, EssenceType.ICE)));
    }
}
