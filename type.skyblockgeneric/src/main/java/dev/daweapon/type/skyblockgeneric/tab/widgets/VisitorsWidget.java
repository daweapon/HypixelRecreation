package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorData;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

final class VisitorsWidget extends AreaWidget {
    VisitorsWidget() {
        super("visitors", "Visitors", Material.VILLAGER_SPAWN_EGG, 65, TabLocation.GARDEN);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        VisitorData visitors = Gardens.profile(player).visitors();
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<b><l>Visitors: <f>({})", visitors.visible().size()));
        for (VisitorData.Present present : visitors.visible()) {
            VisitorCatalogue.Visitor visitor = VisitorCatalogue.get().visitor(present.getId());
            lines.add(Text.of(" {}", Text.parse(visitor == null ? present.getId() : visitor.coloured())));
        }
        long wait = Math.max(0, visitors.getNextVisitorAt() - System.currentTimeMillis());
        lines.add(Text.of(" <7>Next Visitor: <f>{}", wait == 0 ? "Soon" : AreaTabClock.minutes(wait)));
        return lines;
    }
}
