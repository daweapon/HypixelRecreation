package dev.daweapon.type.hub.gui.carnival;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Doug's Souvenir Shop, from the wiki's Doug/UI subpage.
public class GUISouvenirShop extends StatelessView {
    private static final String FROG_MASK_TEXTURE = "87522a1eadf8996f42a8d2e5fb0ca9bfb3ab483d3fbaf68ef29a42de8cef74c5";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Souvenir Shop", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        layout.slot(11, (_, _) -> ItemStacks.head(FROG_MASK_TEXTURE, """
                <a>Mask Shop
                <7>Exchange your <e>Carnival Tokens <7>for
                <7>masks, which grant you <b>small <7>and
                <b>not-so-small <7>buffs while worn!

                <e>Click to view!"""), (_, c) -> c.push(new GUICarnivalMasks()));
        layout.slot(15, (_, _) -> ItemStacks.item(Material.CLOCK, """
                <a>Event Shop
                <7>Exchange your <e>Carnival Tokens <7>for
                <7>event buffs across all of <b>SkyBlock<7>!

                <e>Click to view!"""), (_, c) -> c.push(new GUICarnivalPerks()));
        layout.slot(32, (_, c) -> CarnivalTokensDisplay.of((SkyBlockPlayer) c.player()));
    }
}
