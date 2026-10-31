package dev.daweapon.type.theend.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
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
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.EssenceGuideEntry;
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.GUIEssenceShop;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUITyzzo extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Dragon Ritualist", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(20, ItemStacks.item(Material.CRYING_OBSIDIAN, """
                <c>Draconic Altar Guide
                <7>View a list of <d>End <7>and <5>Dragon <7>items
                <7>which can be sacrificed in the altar.

                <7>Sacrificing an item will give you
                <7><d>Dragon Essence <7>and a small chance
                <7>for <c>bonus items<7>.

                <e>Click to view!"""), (_, c) -> c.push(new GUIDraconicAltarGuide(0)));
        layout.slot(22, ItemStacks.item(Material.BEACON, """
                <a>Salvage Items
                <7>Salvage weapons and armor for
                <7>materials.

                <c>Salvaging isn't in this server yet."""));
        layout.slot(24, (_, c) -> shopButton((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUIEssenceShop(EssenceType.DRAGON)));
    }

    private static ItemStack.Builder shopButton(SkyBlockPlayer player) {
        return ItemStacks.head(EssenceGuideEntry.of(EssenceType.DRAGON).texture(), """
                <a>Dragon Essence Shop
                <7>Exchange <d>Dragon Essence <7>for perks
                <7>useful in <d>The End <7>and beyond!

                <7>Your Dragon Essence: <d>{}

                <e>Click to view!""", StringUtility.commaify(Essences.get(player, EssenceType.DRAGON)));
    }
}
