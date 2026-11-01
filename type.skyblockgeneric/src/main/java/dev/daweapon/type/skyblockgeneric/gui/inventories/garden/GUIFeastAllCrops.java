package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import dev.daweapon.type.skyblockgeneric.garden.feast.FeastCalendar;

import java.util.ArrayList;
import java.util.List;

public class GUIFeastAllCrops extends StatelessView {
    private static final int[] SEASON_SLOTS = {11, 12, 14, 15};
    private static final int[] OFF_SEASON_SLOTS = {28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("All Crops Harvest"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        List<GardenCrop> inSeason = FeastCalendar.inSeasonNow();
        List<GardenCrop> others = new ArrayList<>(List.of(GardenCrop.values()));
        others.removeAll(inSeason);
        for (int i = 0; i < SEASON_SLOTS.length && i < inSeason.size(); i++) {
            GardenCrop crop = inSeason.get(i);
            layout.slot(SEASON_SLOTS[i], (_, _) -> GUIFeast.cropItem(crop, true));
        }
        for (int i = 0; i < OFF_SEASON_SLOTS.length && i < others.size(); i++) {
            GardenCrop crop = others.get(i);
            layout.slot(OFF_SEASON_SLOTS[i], (_, _) -> GUIFeast.cropItem(crop, false));
        }
    }
}
