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
        java.util.Map<String, Integer> grouped = new java.util.LinkedHashMap<>();
        for (IslandMinionData.IslandMinion minion : minions) {
            String state = switch (minion.getInternalMinionTags().getState()) {
                case BAD_FULL -> "<c>FULL";
                case BAD_LOCATION -> "<c>BLOCKED";
                default -> "<a>ACTIVE";
            };
            String key = minion.getMinion().getDisplay().replace(" Minion", "") + "\u0000" + minion.getTier() + "\u0000" + state;
            grouped.merge(key, 1, Integer::sum);
        }
        for (java.util.Map.Entry<String, Integer> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split("\u0000");
            lines.add(Text.of(" <7>{}x {} {:roman} {}", entry.getValue(), parts[0],
                    Integer.parseInt(parts[1]), Text.parse("<7>[" + parts[2] + "<7>]")));
        }
        return lines;
    }
}
