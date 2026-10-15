package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.starter;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.v2.*;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelCause;
import dev.daweapon.type.skyblockgeneric.levels.causes.NewAccessoryLevelCause;

import java.util.List;
import java.util.stream.Stream;

// GUI intended to show starter accessory unlocks for given level-up causes (content not yet implemented)
public class GUIStarterAccessories extends StatelessView {
    private final List<NewAccessoryLevelCause> causes;

    public GUIStarterAccessories(ItemType... causes) {
        this.causes = Stream.of(causes).map(SkyBlockLevelCause::getAccessoryCause).toList();
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Starter -> Accessories", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        // TODO: Add accessory-related content using causes
    }
}
