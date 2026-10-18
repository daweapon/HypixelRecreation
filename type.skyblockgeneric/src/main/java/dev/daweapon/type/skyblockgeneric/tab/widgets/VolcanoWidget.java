package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoExplosivity;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

final class VolcanoWidget extends AreaWidget {
    VolcanoWidget() {
        super("volcano", "Volcano", Material.MAGMA_BLOCK, 50, TabLocation.CRIMSON_ISLE);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        VolcanoExplosivity explosivity = VolcanoEruptionService.currentExplosivity();
        return List.of(Text.parse("<c><l>Volcano: " + (explosivity == null ? "<7>INACTIVE" : explosivity.coloured())));
    }
}
