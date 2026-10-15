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

public class GUIHookGuide extends StatelessView {
    private static final int[] PART_SLOTS = {10, 11, 12, 13, 14, 15, 16};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Hook", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);
        layout.slot(4, ItemStacks.head(
            "9809753cbab0380c7a1c18925faf9b51e44caadd1e5748542b0f23835f4ef64e", """
                <9>ථ Hooks
                <9>Hooks <7>change what your rod is better at catching."""));

        int index = 0;
        for (var part : FishingItemSupport.getRodParts()) {
            if (part.getComponent(dev.daweapon.type.skyblockgeneric.item.components.FishingRodPartComponent.class).getCategory() != FishingPartCategory.HOOK || index >= PART_SLOTS.length) {
                continue;
            }
            layout.slot(PART_SLOTS[index++], FishingGuideStackFactory.buildRodPartStack(part));
        }
        layout.slot(48, ItemStacks.item(Material.ARROW, """
                <a>Go Back
                <7>To Rod Part Guide"""), (_, viewCtx) -> viewCtx.pop());
    }
}
