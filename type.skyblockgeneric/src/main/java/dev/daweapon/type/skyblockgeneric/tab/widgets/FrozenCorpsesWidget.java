package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseService;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

final class FrozenCorpsesWidget extends AreaWidget {
    FrozenCorpsesWidget() {
        super("frozen_corpses", "Frozen Corpses", Material.PACKED_ICE, 85, TabLocation.DWARVEN_MINES);
    }

    @Override
    public String description() {
        return "Shows which Frozen Corpses in your Glacite Mineshaft are looted.";
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        return CorpseService.widgetLines(player);
    }
}
