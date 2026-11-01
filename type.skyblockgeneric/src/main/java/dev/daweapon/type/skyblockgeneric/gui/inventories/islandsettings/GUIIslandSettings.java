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
        return ViewConfiguration.withString((state, context) -> "Island Management", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        layout.slot(11, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            IslandPrivacy tier = IslandPrivacy.fromStored(player.getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.ISLAND_PRIVACY, DatapointString.class).getValue());
            return ItemStacks.item(Material.OAK_DOOR, "<a>Island Privacy\n<7>Control who can /visit your island.\n\n<7>Currently: <e>{}",
                    label(tier));
        }, (click, c) -> c.push(new GUIIslandSettingsPrivacy()));

        layout.slot(13, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            int ignored = player.getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.IGNORED_VISITORS, DatapointStringList.class).getValue().size();
            return ItemStacks.item(Material.OAK_FENCE,
                    "<a>Ignored Players\n<7>Players blocked from visiting your\n<7>island, regardless of privacy.\n\n<7>Currently ignoring: <e>{}",
                    ignored);
        }, (click, c) -> c.push(new GUIIslandSettingsIgnored()));

        layout.slot(33, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            int visitors = VisitManager.visitorIds(player.getSkyBlockIsland().getIslandID()).size();
            int limit = VisitManager.maxGuestLimit(player.getProfiles().getCurrentlySelected(), player.getUuid());
            return ItemStacks.item(Material.SHORT_GRASS,
                    "<a>Guests Management\n<7>Manage players guesting on your\n<7>island.\n\n<a>✌ <7>Guests: <a>{}<7>/{}\n\n<e>Click to view!",
                    visitors, limit);
        }, (click, c) -> c.push(new GUIIslandSettingsGuests()));

        layout.slot(15, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
            String summary = coop == null
                    ? "<7>You aren't in a co-op right now."
                    : "<7>Members: <e>" + coop.members().size() + " <7>/ Invited: <e>" + coop.memberInvites().size();
            return ItemStacks.item(Material.PLAYER_HEAD, "<a>Co-op Members\n<7>View your co-op roster and vote\n<7>to kick a member.\n\n"
                    + summary);
        }, (click, c) -> c.push(new GUIIslandSettingsCoop()));

        layout.slot(29, (s, c) -> ItemStacks.item(Material.NETHER_STAR,
                "<a>Island Properties\n<7>Modify island time, mob spawns,\n<7>biomes, and more.\n\n<e>Click to view!"),
                (click, c) -> c.push(new GUIIslandProperties()));

        layout.slot(31, (s, c) -> ItemStacks.head(((SkyBlockPlayer) c.player()).getPlayerSkin(),
                "<7>{}'s Island\n<7>Tags: <8>None", ((SkyBlockPlayer) c.player()).getUsername()));

        Components.backOrClose(layout, 48, ctx);
        Components.close(layout, 49);
    }

    static String label(IslandPrivacy tier) {
        return switch (tier) {
            case PUBLIC -> "Public";
            case FRIENDS_ONLY -> "Friends Only";
            case PRIVATE -> "Private";
        };
    }
}
