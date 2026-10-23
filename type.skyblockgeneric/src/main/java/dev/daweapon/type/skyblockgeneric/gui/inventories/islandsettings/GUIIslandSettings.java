package dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.generic.data.datapoints.DatapointStringList;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.IslandPrivacy;
import dev.daweapon.type.skyblockgeneric.user.island.VisitManager;

// Central hub for managing a player's island - visitation privacy, blocked visitors, current
// guests, and co-op roster. Previously each of these existed only as separate commands
// (/islandprivacy, /ignore, /sbkick(all), /coopkick) with no unified GUI, and GUIGuests' "Access
// Permissions"/"Moderation" buttons only echoed command reminders rather than actually opening
// anything (Roadmap #23 / Bugs & Loose Ends #77).
public class GUIIslandSettings extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Island Settings", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        layout.slot(10, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            IslandPrivacy tier = IslandPrivacy.fromStored(player.getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.ISLAND_PRIVACY, DatapointString.class).getValue());
            return ItemStacks.item(Material.OAK_DOOR, "<a>Island Privacy\n<7>Control who can /visit your island.\n\n<7>Currently: <e>{}",
                    label(tier));
        }, (click, c) -> c.push(new GUIIslandSettingsPrivacy()));

        layout.slot(12, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            int ignored = player.getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.IGNORED_VISITORS, DatapointStringList.class).getValue().size();
            return ItemStacks.item(Material.OAK_FENCE,
                    "<a>Ignored Players\n<7>Players blocked from visiting your\n<7>island, regardless of privacy.\n\n<7>Currently ignoring: <e>{}",
                    ignored);
        }, (click, c) -> c.push(new GUIIslandSettingsIgnored()));

        layout.slot(14, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            int visitors = VisitManager.visitorIds(player.getSkyBlockIsland().getIslandID()).size();
            int limit = VisitManager.maxGuestLimit(player.getProfiles().getCurrentlySelected(), player.getUuid());
            return ItemStacks.item(Material.SHORT_GRASS,
                    "<a>Guest Management\n<7>View and kick players currently\n<7>visiting your island.\n\n<7>Guests: <e>{}<7>/<e>{}",
                    visitors, limit);
        }, (click, c) -> c.push(new GUIIslandSettingsGuests()));

        layout.slot(16, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
            String summary = coop == null
                    ? "<7>You aren't in a co-op right now."
                    : "<7>Members: <e>" + coop.members().size() + " <7>/ Invited: <e>" + coop.memberInvites().size();
            return ItemStacks.item(Material.PLAYER_HEAD, "<a>Co-op Members\n<7>View your co-op roster and vote\n<7>to kick a member.\n\n"
                    + summary);
        }, (click, c) -> c.push(new GUIIslandSettingsCoop()));

        layout.slot(22, (s, c) -> ItemStacks.item(Material.COMPARATOR,
                "<a>Island Properties\n<7>Weather and biome of your island.\n\n<e>Click to open!"),
                (click, c) -> c.push(new GUIIslandProperties()));

        Components.backOrClose(layout, 31, ctx);
    }

    static String label(IslandPrivacy tier) {
        return switch (tier) {
            case PUBLIC -> "Public";
            case FRIENDS_ONLY -> "Friends Only";
            case PRIVATE -> "Private";
        };
    }
}
