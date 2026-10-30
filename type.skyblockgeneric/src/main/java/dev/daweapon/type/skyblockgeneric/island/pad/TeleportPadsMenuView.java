package dev.daweapon.type.skyblockgeneric.island.pad;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewNavigator;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.List;

public class TeleportPadsMenuView extends StatelessView {
    private static final int PAGE_SIZE = 45;
    private static final int PREVIOUS_SLOT = 45;
    private static final int CLOSE_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    private final SkyBlockIsland island;
    private final int page;

    public TeleportPadsMenuView(SkyBlockIsland island) {
        this(island, 0);
    }

    private TeleportPadsMenuView(SkyBlockIsland island, int page) {
        this.island = island;
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Teleport Pads", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        List<TeleportPad> pads = TeleportPads.of(island).all();
        int first = page * PAGE_SIZE;
        for (int slot = 0; slot < PAGE_SIZE && first + slot < pads.size(); slot++) {
            TeleportPad pad = pads.get(first + slot);
            layout.slot(slot, (_, _) -> ItemStacks.item(pad.iconType().material(), """
                    %s
                    <7>Click to teleport to this
                    <7>Teleport Pad!

                    <e>Click to teleport!""".formatted(pad.displayName())), (_, c) -> teleport((SkyBlockPlayer) c.player(), pad));
        }
        Components.close(layout, CLOSE_SLOT);
        if (page > 0) {
            layout.slot(PREVIOUS_SLOT, (_, _) -> ItemStacks.item(Material.ARROW, "<a>Previous Page"),
                    (_, c) -> ViewNavigator.get(c.player()).replace(new TeleportPadsMenuView(island, page - 1)));
        }
        if (first + PAGE_SIZE < pads.size()) {
            layout.slot(NEXT_SLOT, (_, _) -> ItemStacks.item(Material.ARROW, "<a>Next Page"),
                    (_, c) -> ViewNavigator.get(c.player()).replace(new TeleportPadsMenuView(island, page + 1)));
        }
    }

    private void teleport(SkyBlockPlayer player, TeleportPad pad) {
        player.closeInventory();
        if (TeleporterPill.ownIslandOf(player) != island || TeleportPads.of(island).at(pad.x(), pad.y(), pad.z()) == null) {
            player.sendMessage("<c>You can't teleport there right now!");
            return;
        }
        TeleportPads.arrive(player, pad, pad.direction());
        player.sendMessage("<a>Teleported to {}<a>!", pad.displayName());
    }
}
