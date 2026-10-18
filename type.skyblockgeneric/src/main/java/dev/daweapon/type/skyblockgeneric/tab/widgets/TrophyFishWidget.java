package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.fishing.trophy.TrophyFishTabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

final class TrophyFishWidget extends AreaWidget {
    TrophyFishWidget() {
        super("trophy_fish", "Trophy Fish", Material.FISHING_ROD, 60, TabLocation.CRIMSON_ISLE);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        return TrophyFishTabWidget.lines(player).stream().map(Text::parse).toList();
    }
}
