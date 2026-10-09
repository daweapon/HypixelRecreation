package net.swofty.type.skyblockgeneric.gui.inventories.kuudra;

import net.minestom.server.inventory.InventoryType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** Shown to a party member who picks a tier (the Burning and Infernal captures). */
public class GUIKuudraNeedPartyLeader extends StatelessView {
    private static final String TEXTURE = "ff94b7e38d8a2d4049e7b1eb43fca78ab424c56980ec4b8debcba10347a726e7";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Need party leader!", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(13, ItemStacks.head(TEXTURE, """
                <c>Need party leader!
                <7>Ask the leader to setup the instance,
                <7>or create your OWN <9>/party<7>!

                <e>I'm on it!"""), (_, c) -> c.player().closeInventory());
        layout.slot(14, GUICragSelectTier.findAParty(), (_, c) -> GUIKuudraPartyFinder.open((SkyBlockPlayer) c.player()));
        Components.close(layout, 31);
    }
}
