package dev.daweapon.type.skyblockgeneric.user.island.portal;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.function.Consumer;

public class PortalDeleteConfirmView extends StatelessView {
    private final SkyBlockIsland island;
    private final Consumer<SkyBlockPlayer> onConfirm;

    public PortalDeleteConfirmView(SkyBlockIsland island, Consumer<SkyBlockPlayer> onConfirm) {
        this.island = island;
        this.onConfirm = onConfirm;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Destroy the Portal?", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.slot(11, (_, _) -> ItemStacks.item(Material.GREEN_TERRACOTTA, """
                        <a>Confirm
                        <7>This will remove all of the portal's
                        <7>portal blocks. This WILL NOT break
                        <7>the portal's frame!"""),
                (_, c) -> {
                    c.player().closeInventory();
                    onConfirm.accept((SkyBlockPlayer) c.player());
                });
        layout.slot(15, (_, _) -> ItemStacks.item(Material.RED_TERRACOTTA, "<c>Cancel"),
                (_, c) -> c.player().closeInventory());
        layout.slot(30, (_, _) -> ItemStacks.item(Material.OAK_SIGN, """
                        <c>Don't show the hologram?
                        <7>Click to toggle the "Click HERE to
                        <7>remove" hologram from displaying
                        <7>below the portal's destination
                        <7>hologram.

                        <e>Click to toggle!"""),
                (_, c) -> toggleRemoveHint(c));
        Components.close(layout, 31);
    }

    private void toggleRemoveHint(ViewContext context) {
        IslandPortals portals = island.getPortals();
        if (portals == null || island.getIslandInstance() == null) return;
        boolean hidden = !portals.removeHintHidden();
        portals.setRemoveHintHidden(hidden);
        portals.refreshHolograms(island.getIslandInstance());
        Thread.startVirtualThread(() -> island.getDatabase().insertOrUpdate(IslandPortals.REMOVE_HINT_HIDDEN_KEY, hidden));
        context.player().sendMessage(hidden
                ? "<e>The portal removal hologram is now hidden."
                : "<e>The portal removal hologram is now shown.");
    }
}
