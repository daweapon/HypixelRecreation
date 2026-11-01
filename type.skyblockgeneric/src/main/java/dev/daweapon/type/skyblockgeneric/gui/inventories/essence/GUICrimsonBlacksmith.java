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
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Igor and Alwin's blacksmith menu: essence upgrades, salvaging and the Kuudra Armor Type Transfer.
public class GUICrimsonBlacksmith extends StatelessView {
    private final String title;

    public GUICrimsonBlacksmith(String title) {
        this.title = title;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(title, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(19, ItemStacks.item(Material.DIAMOND_SWORD, """
                <a>Upgrade Items
                <7>Upgrade items using materials and
                <d>Essence <7>to improve their stats!

                <e>Click to view!"""), (_, c) -> new GUIEssenceUpgrade().open((SkyBlockPlayer) c.player()));
        layout.slot(22, ItemStacks.item(Material.BEACON, """
                <a>Salvage Items
                <7>Salvage weapons and armor for
                <7>materials.

                <e>Click to view!"""), (_, c) -> new GUIEssenceSalvage().open((SkyBlockPlayer) c.player()));
        layout.slot(25, ItemStacks.item(Material.BEACON, """
                <8>Kuudra Armor Type Transfer
                <7>Combine two <b>Kuudra Armors <7>to
                <7>increase their tier.
                """ + KuudraAvailability.UNAVAILABLE_LORE),
                (_, c) -> ((SkyBlockPlayer) c.player()).sendMessage(KuudraAvailability.UNAVAILABLE_CHAT));
        layout.slot(50, (_, c) -> ItemStacks.head(EssenceGuideEntry.of(EssenceType.CRIMSON).texture(), """
                <a>Essence Info
                <d>Crimson Essence <7>can be used to
                <7>upgrade items at the Blacksmith.

                <7>Your Crimson Essence: <d>{}""", StringUtility.commaify(Essences.get((SkyBlockPlayer) c.player(), EssenceType.CRIMSON))));
    }
}
