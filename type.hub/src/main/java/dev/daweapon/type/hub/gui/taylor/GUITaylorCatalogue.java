package dev.daweapon.type.hub.gui.taylor;

import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;

public class GUITaylorCatalogue extends CatalogueShopView {

    public GUITaylorCatalogue(String catalogueId) {
        super(catalogueId);
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        TaylorGems.slot(layout, 50);
    }

    @Override
    public boolean onBottomClick(ClickContext<State> click, ViewContext ctx) {
        return true;
    }
}
