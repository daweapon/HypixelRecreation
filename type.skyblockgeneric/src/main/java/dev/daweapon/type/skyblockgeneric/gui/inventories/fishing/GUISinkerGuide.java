package dev.daweapon.type.skyblockgeneric.gui.inventories.fishing;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.fishing.item.FishingItemSupport;
import dev.daweapon.type.skyblockgeneric.fishing.rod.FishingPartCategory;

// GUI listing available fishing rod sinkers and their effects
public class GUISinkerGuide extends StatelessView {
    private static final int[] PART_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Sinker", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);
        layout.slot(4, ItemStacks.head(
            "d24892a3142d2e130e5feb88b805b83de905489d2ccd1d031b9d7a2922b96500", """
                <9>࿉ Sinkers
                <9>Sinkers <7>add special fishing effects to your rod."""));

        int index = 0;
        for (var part : FishingItemSupport.getRodParts()) {
            if (part.getComponent(dev.daweapon.type.skyblockgeneric.item.components.FishingRodPartComponent.class).getCategory() != FishingPartCategory.SINKER || index >= PART_SLOTS.length) {
                continue;
            }
            layout.slot(PART_SLOTS[index++], FishingGuideStackFactory.buildRodPartStack(part));
        }
        layout.slot(48, ItemStacks.item(Material.ARROW, """
                <a>Go Back
                <7>To Rod Part Guide"""), (_, viewCtx) -> viewCtx.pop());
    }
}
