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
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseConfig;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseEffect;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.PlantType;

import java.util.ArrayList;
import java.util.List;

public class GUICropGuide extends StatelessView {
    private static final int[] SLOTS = {19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32, 33};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Crop Guide"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        List<PlantType> crops = GreenhouseConfig.get().plants().stream().filter(PlantType::baseCrop)
                .filter(plant -> plant.crop() != GardenCrop.MOONFLOWER).toList();
        for (int i = 0; i < crops.size() && i < SLOTS.length; i++) {
            PlantType crop = crops.get(i);
            layout.slot(SLOTS[i], (_, _) -> {
                List<Text> lore = new ArrayList<>(List.of(
                        Text.of("<8>Base Crop"),
                        Text.empty(),
                        Text.of("<7>Size: <a>1<7>x<a>1"),
                        Text.of("<7>Growth Surface: <a>{}", crop.surface()),
                        Text.of("<7>Growth Stages: <a>{}", crop.stages()),
                        Text.empty(),
                        Text.of("<6>Drops"),
                        Text.of("<7>{}x <f>{}", crop.baseYield(), dropName(crop)),
                        Text.empty(),
                        Text.of("<6>Effects")));
                for (GreenhouseEffect effect : crop.effects()) {
                    lore.add(Text.of("<8>➤ {}{}", effect.isNegative() ? "<c>" : "<a>", effect.getDisplayName()));
                    for (String line : wrap(effect.getDescription(), 34)) {
                        lore.add(Text.of("<7>{}", line));
                    }
                }
                return ItemStacks.item(GardenMenus.icon(crop.crop()), Text.of("<a>" + displayName(crop)), lore);
            });
        }
    }

    private static String displayName(PlantType crop) {
        return switch (crop.crop()) {
            case SUNFLOWER -> "Sunflower & Moonflower";
            case MELON -> "Melon";
            case MUSHROOM -> "Mushrooms";
            default -> crop.name();
        };
    }

    private static String dropName(PlantType crop) {
        return switch (crop.crop()) {
            case MUSHROOM -> "Red Mushroom";
            case MELON -> "Melon Slice";
            default -> crop.name();
        };
    }

    private static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (!line.isEmpty() && line.length() + word.length() + 1 > width) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (!line.isEmpty()) line.append(' ');
            line.append(word);
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }
}
