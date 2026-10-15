package dev.daweapon.type.skyblockgeneric.gui.inventories.essence;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Igor and Alwin's blacksmith menu: essence upgrades, salvaging and the Kuudra Armor Type Transfer.
// The wiki has no slot layout for this menu, so the three options follow the centred-row rule.
public class GUICrimsonBlacksmith extends StatelessView {
    private final String title;

    public GUICrimsonBlacksmith(String title) {
        this.title = title;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(title, InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        layout.slot(11, ItemStacks.item(Material.ANVIL, """
                <a>Upgrade Items
                <7>Use <d>Essence <7>to add stars to your
                <7>gear. Each star grants <a>+2% <7>of the
                <7>item's stats.

                <e>Click to open!"""), (_, c) -> new GUIEssenceUpgrade().open((SkyBlockPlayer) c.player()));
        layout.slot(13, ItemStacks.item(Material.GRINDSTONE, """
                <a>Salvage Items
                <7>Break down eligible gear into
                <d>Essence<7>. Upgraded items refund half
                <7>of their star costs.

                <e>Click to open!"""), (_, c) -> new GUIEssenceSalvage().open((SkyBlockPlayer) c.player()));
        layout.slot(15, ItemStacks.item(Material.SMITHING_TABLE, """
                <8>Kuudra Armor Type Transfer
                <7>Sacrifice a Kuudra Armor into the
                <7>target item.

                """ + KuudraAvailability.UNAVAILABLE_LORE),
                (_, c) -> ((SkyBlockPlayer) c.player()).sendMessage(KuudraAvailability.UNAVAILABLE_CHAT));
    }
}
