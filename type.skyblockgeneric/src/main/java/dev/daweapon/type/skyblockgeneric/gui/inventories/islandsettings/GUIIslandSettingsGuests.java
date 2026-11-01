package dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.VisitManager;

import java.util.List;
import java.util.Set;
import java.util.UUID;

// Shows who is currently visiting this island, with a per-guest kick button and a kick-all
// button. Replaces GUIGuests' old "Moderation" button, which only offered mass-kick with no
// roster view (Roadmap #23 / Bugs & Loose Ends #77). Kicks are dispatched as the underlying
// /sbkick(all) commands rather than calling VisitManager directly, reusing their existing
// isVisiting/empty-guest-list guard checks and messaging instead of duplicating them here.
public class GUIIslandSettingsGuests extends StatelessView {

    private static final int[] GRID_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Guests Management", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        UUID islandId = player.getSkyBlockIsland().getIslandID();
        Set<UUID> visitorIds = VisitManager.visitorIds(islandId);
        int limit = VisitManager.maxGuestLimit(player.getProfiles().getCurrentlySelected(), player.getUuid());

        List<SkyBlockPlayer> visitors = SkyBlockGenericLoader.getLoadedPlayers().stream()
                .filter(loaded -> visitorIds.contains(loaded.getUuid()))
                .toList();


        for (int i = 0; i < visitors.size() && i < GRID_SLOTS.length; i++) {
            SkyBlockPlayer visitor = visitors.get(i);
            layout.slot(GRID_SLOTS[i], (s, c) -> ItemStacks.head(visitor.getPlayerSkin(),
                    "<7>{}\n<7>Currently visiting your island.\n\n<c>Click to kick!", visitor.getUsername()),
                    (click, c) -> {
                        SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                        clicker.closeInventory();
                        MinecraftServer.getCommandManager().execute(clicker, "sbkick " + visitor.getUsername());
                    });
        }

        layout.slot(23, (s, c) -> ItemStacks.item(Material.REPEATER,
                "<a>Kick All Guests\n<7>Get rid of those pesky guests all at\n<7>the same time.\n\n<7>Command: <b>/sbkickall\n\n<e>Click to kick all guests!"),
                (click, c) -> {
                    SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                    clicker.closeInventory();
                    MinecraftServer.getCommandManager().execute(clicker, "sbkickall");
                });

        Components.backOrClose(layout, 22, ctx);
    }
}
