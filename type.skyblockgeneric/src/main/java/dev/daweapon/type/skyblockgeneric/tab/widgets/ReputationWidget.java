package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.faction.ReputationTier;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

final class ReputationWidget extends AreaWidget {
    ReputationWidget() {
        super("reputation", "Reputation", Material.NETHER_STAR, 80, TabLocation.CRIMSON_ISLE);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        if (faction == null) {
            return List.of(Text.parse("<a><l>Reputation:"), Text.parse(" <7>Join a faction through <d>Elle<7>."));
        }
        int reputation = CrimsonReputationService.get(player, faction);
        ReputationTier tier = ReputationTier.of(reputation);
        ReputationTier next = tier.next();
        return List.of(
                Text.parse(faction.getColour() + "<l>" + faction.getDisplayName() + " Reputation:"),
                Text.parse(" " + tier.coloured() + " <7>" + StringUtility.commaify(reputation)
                        + (next == null ? "" : "<8>/" + StringUtility.commaify(next.getRequiredReputation()))));
    }
}
