package net.swofty.type.hub.gui;

import net.minestom.server.inventory.InventoryType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;

/**
 * Ozanne only refunds reforges the Blacksmith applied to accessories before April 2022. No
 * accessory on this server carries such a legacy reforge, so there is never anything to offer.
 */
public class GUIOzanne extends StatelessView {
    private static final String ACCESSORY_BAG_TEXTURE = "396ce13ff6155fdf3235d8d22174c5de4bf5512f1adeda1afa3fc28180f3f7";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Ozanne", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        layout.slot(13, ItemStacks.head(ACCESSORY_BAG_TEXTURE, """
                <a>Sell Reforges On Accessories
                <7>Ozanne will remove and refund
                <7>reforges from accessories in
                <7>your Accessory Bag.
                <6>---------------------------
                <f><l>COMMON</l><7>: <6>2,500 coins
                <a><l>UNCOMMON</l><7>: <6>5,000 coins
                <9><l>RARE</l><7>: <6>10,000 coins
                <5><l>EPIC</l><7>: <6>25,000 coins
                <6><l>LEGENDARY</l><7>: <6>50,000 coins
                <d><l>MYTHIC</l><7>: <6>100,000 coins

                <9>Sweet<7>: <6>40,000 coins
                <9>Silky<7>: <6>50,000 coins
                <9>Bloody<7>: <6>250,000 coins
                <9>Shaded<7>: <6>1,000,000 coins
                <6>---------------------------

                <e>You have nothing to offer!"""),
                (click, c) -> c.player().sendMessage("<c>You have nothing to offer!"));
    }
}
