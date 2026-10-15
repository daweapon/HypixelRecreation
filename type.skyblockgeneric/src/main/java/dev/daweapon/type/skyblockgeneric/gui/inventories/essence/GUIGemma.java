package dev.daweapon.type.skyblockgeneric.gui.inventories.essence;

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
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Gemma's menu, from the wiki's Gemma/UI page: Salvage Items on the left, the Diamond Essence Shop on the right.
public class GUIGemma extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Gemma", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(20, ItemStacks.item(Material.BEACON, """
                <a>Salvage Items
                <7>Salvage weapons and armor for materials.

                <e>Click to view!"""), (_, c) -> new GUIEssenceSalvage().open((SkyBlockPlayer) c.player()));
        layout.slot(24, (_, c) -> shopButton((SkyBlockPlayer) c.player()),
                (_, c) -> c.push(new GUIEssenceShop(EssenceType.DIAMOND)));
    }

    private static ItemStack.Builder shopButton(SkyBlockPlayer player) {
        return ItemStacks.head(EssenceGuideEntry.of(EssenceType.DIAMOND).texture(), """
                <a>Diamond Essence Shop
                <7>Exchange <b>Diamond Essence <7>for
                <7>perks useful in the <5>Crystal
                <5>Hollows <7>and beyond!

                <7>Your Diamond Essence: <d>{}

                <e>Click to view!""", StringUtility.commaify(Essences.get(player, EssenceType.DIAMOND)));
    }
}
