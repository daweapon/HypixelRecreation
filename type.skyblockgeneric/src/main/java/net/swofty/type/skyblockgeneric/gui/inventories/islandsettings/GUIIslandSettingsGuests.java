package net.swofty.type.skyblockgeneric.gui.inventories.islandsettings;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.island.VisitManager;

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
            19, 20, 21, 22, 23, 24, 25,
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Guest Management", InventoryType.CHEST_5_ROW);
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

        layout.slot(4, (s, c) -> ItemStacks.item(Material.SHORT_GRASS,
                "<a>Guests limit\n<7>You can host up to <e>{}<7> guest(s)\n<7>on your island at once.\n\n<7>Currently visiting: <e>{}",
                limit, visitors.size()));

        if (visitors.isEmpty()) {
            layout.slot(22, (s, c) -> ItemStacks.item(Material.BARRIER, "<c>Nobody is visiting\n<7>Nobody is currently visiting your island."));
        }

        for (int i = 0; i < visitors.size() && i < GRID_SLOTS.length; i++) {
            SkyBlockPlayer visitor = visitors.get(i);
            layout.slot(GRID_SLOTS[i], (s, c) -> ItemStacks.head(visitor.getPlayerSkin(),
                    "<c>{}\n<7>Currently visiting your island.\n\n<c>Click to kick!", visitor.getUsername()),
                    (click, c) -> {
                        SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                        clicker.closeInventory();
                        MinecraftServer.getCommandManager().execute(clicker, "sbkick " + visitor.getUsername());
                    });
        }

        layout.slot(40, (s, c) -> ItemStacks.item(Material.REPEATER,
                "<a>Kick All Guests\n<7>Click to kick every current guest\n<7>off your island at once."),
                (click, c) -> {
                    SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                    clicker.closeInventory();
                    MinecraftServer.getCommandManager().execute(clicker, "sbkickall");
                });

        Components.backOrClose(layout, 44, ctx);
    }
}
