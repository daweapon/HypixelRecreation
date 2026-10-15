package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseConfig;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseEffect;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.PlantType;

import java.util.ArrayList;
import java.util.List;

public class GUICropGuide extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Crop Guide"), InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 40, ctx);
        List<PlantType> crops = GreenhouseConfig.get().plants().stream().filter(PlantType::baseCrop).toList();
        for (int i = 0; i < crops.size() && i < SLOTS.length; i++) {
            PlantType crop = crops.get(i);
            layout.slot(SLOTS[i], (_, _) -> {
                List<Text> lore = new ArrayList<>(List.of(
                        Text.of("<8>Base Crop"),
                        Text.of("<7>Size: <a>1x1"),
                        Text.of("<7>Growth Surface: <a>{}", crop.surface()),
                        Text.of("<7>Growth Stages: <a>{}", crop.stages()),
                        Text.of("<7>Base Yield: <a>{}", crop.baseYield()),
                        Text.empty(),
                        Text.of("<7>Effects:")));
                for (GreenhouseEffect effect : crop.effects()) {
                    lore.add(Text.of("<8>> {}{}<7>: {}", effect.isNegative() ? "<c>" : "<a>", effect.getDisplayName(), effect.getDescription()));
                }
                return ItemStacks.item(GardenMenus.icon(crop.crop()), Text.of("<a>" + crop.name()), lore);
            });
        }
    }
}
