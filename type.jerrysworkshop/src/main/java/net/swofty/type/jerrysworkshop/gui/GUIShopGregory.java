package net.swofty.type.jerrysworkshop.gui;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags.QuiverRefill;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Gregory the Opportunist's overpriced shop, open at Jerry Pond while a Reindrake is alive. "Fill
// your Quiver" charges his own Flint Arrow rate (240 coins per 20).
public class GUIShopGregory extends ShopView {
    private static final int ARROW_BUNDLE = 20;
    private static final int ARROW_BUNDLE_PRICE = 240;
    private static final double COINS_PER_ARROW = (double) ARROW_BUNDLE_PRICE / ARROW_BUNDLE;
    private static final int FILL_QUIVER_SLOT = 53;

    public GUIShopGregory() {
        super("Gregory the Opportunist", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(new ShopItem(new SkyBlockItem(ItemType.FLINT_ARROW), ARROW_BUNDLE,
                new CoinShopPrice(ARROW_BUNDLE_PRICE), false, false));
        attachItem(new ShopItem(new SkyBlockItem(ItemType.DECENT_BOW), 1, new CoinShopPrice(500), false, false));
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        super.layoutCustom(layout, state, ctx);
        layout.slot(FILL_QUIVER_SLOT, (s, c) -> {
            int missing = QuiverRefill.missingArrows((SkyBlockPlayer) c.player(), ItemType.FLINT_ARROW);
            return ItemStacks.item(Material.ARROW, """
                    <a>Fill your Quiver
                    <7>Fills every slot of your Quiver
                    <7>with <f>Flint Arrows<7>.

                    <7>Cost
                    <6>{} Coins

                    <e>Click to fill!""", Math.round(missing * COINS_PER_ARROW));
        }, (click, c) -> {
            QuiverRefill.fill((SkyBlockPlayer) c.player(), ItemType.FLINT_ARROW, COINS_PER_ARROW);
            c.session(Object.class).refresh();
        });
    }
}
