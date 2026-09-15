package net.swofty.type.skyblockgeneric.gui.inventories.islandsettings;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.UUID;

// Shows the players blocked via /ignore add from visiting this island, with a per-entry remove
// button. Adding a new entry still requires /ignore add <player> - typing a username isn't
// practical from a chest GUI, and GUIGuests' old "Access Permissions" button already pointed
// players at that command instead of a real menu.
public class GUIIslandSettingsIgnored extends StatelessView {

    private static final int[] GRID_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Ignored Players", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        List<String> ignored = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.IGNORED_VISITORS, DatapointStringList.class).getValue();

        if (ignored.isEmpty()) {
            layout.slot(22, (s, c) -> ItemStacks.item(Material.BARRIER,
                    "<c>Nobody is ignored\n<7>Use /ignore add \\<player> to block\n<7>a specific player from visiting."));
        }

        for (int i = 0; i < ignored.size() && i < GRID_SLOTS.length; i++) {
            String raw = ignored.get(i);
            layout.slot(GRID_SLOTS[i], (s, c) -> {
                UUID target = safeUuid(raw);
                if (target == null) {
                    return ItemStacks.item(Material.BARRIER, "<c>Unknown Player\n<7>Click to remove this invalid entry.");
                }
                String name = SkyBlockPlayer.getRawName(target);
                return ItemStacks.head(PlayerSkin.fromUuid(target.toString()),
                        "<c>{}\n<7>Blocked from visiting your island.\n\n<c>Click to unblock!", name);
            }, (click, c) -> {
                SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                UUID target = safeUuid(raw);
                String name = target == null ? null : SkyBlockPlayer.getRawName(target);

                SkyBlockDataHandler handler = clicker.getSkyblockDataHandler();
                DatapointStringList datapoint = handler.get(SkyBlockDataHandler.Data.IGNORED_VISITORS, DatapointStringList.class);
                List<String> value = datapoint.getValue();
                value.remove(raw);
                datapoint.setValue(value);
                handler.saveToApi();

                clicker.sendMessage(name == null
                        ? "<e>Removed an invalid ignored-player entry."
                        : "<e>{} can now visit your island again.", name);
                clicker.openView(new GUIIslandSettingsIgnored());
            });
        }

        if (ignored.size() > GRID_SLOTS.length) {
            layout.slot(49, (s, c) -> ItemStacks.item(Material.PAPER,
                    "<c>+{} more not shown\n<7>Use /ignore list to see everyone,\n<7>or /ignore remove \\<player> here.",
                    ignored.size() - GRID_SLOTS.length));
        }

        Components.backOrClose(layout, 45, ctx);
    }

    private static UUID safeUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
