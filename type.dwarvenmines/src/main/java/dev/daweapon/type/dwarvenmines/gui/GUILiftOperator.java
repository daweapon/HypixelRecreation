package dev.daweapon.type.dwarvenmines.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.data.datapoints.DatapointStringList;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUILiftOperator extends StatelessView {
    private enum Destination {
        GUNPOWDER_MINES("Gunpowder Mines", Material.GOLD_INGOT, 10),
        LAPIS_QUARRY("Lapis Quarry", Material.LAPIS_LAZULI, 12),
        PIGMENS_DEN("Pigmen's Den", Material.REDSTONE, 14),
        SLIMEHILL("Slimehill", Material.EMERALD, 16),
        DIAMOND_RESERVE("Diamond Reserve", Material.DIAMOND, 29),
        OBSIDIAN_SANCTUARY("Obsidian Sanctuary", Material.OBSIDIAN, 31);

        private final String displayName;
        private final Material icon;
        private final int slot;

        Destination(String displayName, Material icon, int slot) {
            this.displayName = displayName;
            this.icon = icon;
            this.slot = slot;
        }
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Lift", InventoryType.CHEST_6_ROW);
    }

    private static boolean discovered(SkyBlockPlayer player, Destination destination) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.VISITED_REGIONS, DatapointStringList.class)
                .getValue().contains(destination.displayName);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        for (Destination destination : Destination.values()) {
            layout.slot(destination.slot, (_, _) -> ItemStacks.item(destination.icon, "<a>" + destination.displayName
                    + "\n<7>Click to teleport to the\n<b>" + destination.displayName + "<7>!\n\n"
                    + (discovered(player, destination) ? "<e>Click to travel!" : "<c>Not discovered yet!")), (_, c) -> {
                if (!discovered(player, destination)) {
                    player.sendMessage("<c>You have not discovered the " + destination.displayName + " yet!");
                    return;
                }
                c.player().closeInventory();
                player.sendTo(ServerType.SKYBLOCK_DEEP_CAVERNS);
            });
        }
        layout.slot(33, ItemStacks.item(Material.BARRIER, "<a>Dwarven Mines\n<7>You are already in the\n<b>Dwarven Mines<7>!"),
                (_, _) -> player.sendMessage("<c>You are already in the Dwarven Mines!"));
        Components.close(layout, 49);
    }
}
