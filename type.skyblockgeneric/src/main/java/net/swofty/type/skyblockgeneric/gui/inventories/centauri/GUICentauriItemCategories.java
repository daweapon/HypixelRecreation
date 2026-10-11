package net.swofty.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.inventory.InventoryType;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.gui.inventories.GUICreative;

public final class GUICentauriItemCategories implements View<GUICentauriItemCategories.State> {
    static final int[] CATEGORY_SLOTS = {10, 12, 14, 16};

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Item List", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        ItemCategory[] categories = ItemCategory.values();
        for (int i = 0; i < categories.length; i++) {
            ItemCategory category = categories[i];
            layout.slot(CATEGORY_SLOTS[i], (s, c) -> GUICentauri.item("<a>" + category.getTitle(), category.getIcon(),
                            "<7>Browse " + category.getDescription() + ".", " ",
                            "<7>Items: <b>" + ItemCategory.items(category).size(), " ", "<e>Click to view!"),
                    (click, c) -> c.push(new GUICreative(), GUICreative.createInitialState(category, "", 0)));
        }
        Components.backOrClose(layout, 22, ctx);
    }

    public record State() {}
}
