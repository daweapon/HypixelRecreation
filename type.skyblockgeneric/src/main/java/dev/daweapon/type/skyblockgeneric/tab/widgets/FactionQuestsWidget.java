package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestService;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

final class FactionQuestsWidget extends AreaWidget {
    FactionQuestsWidget() {
        super("faction_quests", "Faction Quests", Material.PAPER, 70, TabLocation.CRIMSON_ISLE);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        return FactionQuestService.tabLines(player).stream().map(Text::parse).toList();
    }
}
