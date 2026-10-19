package dev.daweapon.type.hub.gui.taylor;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.taylor.TaylorSeasons;

import java.time.LocalDate;

public class GUITaylor extends StatelessView {
    private static final String TAYLOR_TEXTURE = "18ca0c89bef8c4e7e521b113821ffdf00e7b5eb552a378f9037cbec655dd27c8";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Taylor's Cosmetics", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        TaylorSeasons.Season season = TaylorSeasons.current(LocalDate.now());
        Components.fill(layout);
        Components.close(layout, 49);

        layout.slot(20, ItemStacks.item(Material.NETHER_STAR, """
                <d>Seasonal Bundles
                <7>Every season, <a>Taylor <7>offers a
                <7>selection of <6>new <7>and <a>returning <7>items
                <7>for sale in a season-long bundle.

                <7>Starting <b>Winter 2024<7>, bundles will
                <7>contain items that can return in
                <7>future seasons. Previous <b>bundle
                <7>items <c>will not <7>be returning in future
                <7>bundles.

                <7>Current bundle: <6>{}

                <e>Click to view!""", season.label()), (click, c) -> c.push(new GUITaylorCatalogue(season.shop())));
        layout.slot(24, ItemStacks.head(TAYLOR_TEXTURE, """
                <a>Taylor's Collection
                <7>Crafted by <a>Taylor <7>herself, these
                <7>cosmetics will always be available in
                <7>her collection.

                <e>Click to view!"""), (click, c) -> c.push(new GUITaylorCatalogue("taylor_collection")));
        TaylorGems.slot(layout, 50);
    }
}
