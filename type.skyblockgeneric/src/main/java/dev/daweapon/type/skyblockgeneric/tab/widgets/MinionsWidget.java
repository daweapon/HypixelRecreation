package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMinionData;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.MinionHandler;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

final class MinionsWidget extends AreaWidget {
    MinionsWidget() {
        super("minions", "Minions", Material.COBBLESTONE, 90, TabLocation.PRIVATE_ISLAND);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        List<IslandMinionData.IslandMinion> minions = player.getSkyBlockIsland().getMinionData().getMinions();
        DatapointMinionData.ProfileMinionData data = player.getSkyblockDataHandler().get(
                SkyBlockDataHandler.Data.MINION_DATA, DatapointMinionData.class).getValue();

        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<b><l>Minions: <f>{}<7>/<f>{}", minions.size(), data.getSlots()));
        for (IslandMinionData.IslandMinion minion : minions) {
            String state = switch (minion.getInternalMinionTags().getState()) {
                case BAD_FULL -> "<c>FULL";
                case BAD_LOCATION -> "<c>BLOCKED";
                default -> "<a>ACTIVE";
            };
            lines.add(Text.of(" <7>{} {:roman} {}", minion.getMinion().getDisplay().replace(" Minion", ""),
                    minion.getTier(), Text.parse(state)));
        }
        return lines;
    }
}
