package dev.daweapon.type.skyblockgeneric.island.pad;

import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewNavigator;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

public class TeleportPadView extends StatelessView {
    private static final int DESTINATION_SLOT = 10;
    private static final int DIRECTION_SLOT = 12;
    private static final int ICON_SLOT = 14;
    private static final int NAME_SLOT = 16;
    private static final int CLOSE_SLOT = 31;
    private static final int PICKUP_SLOT = 35;

    private final SkyBlockIsland island;
    private final TeleportPad pad;

    public TeleportPadView(SkyBlockIsland island, TeleportPad pad) {
        this.island = island;
        this.pad = pad;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Teleport Pad Settings", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(DESTINATION_SLOT, (_, _) -> destinationItem(), (_, c) ->
                ViewNavigator.get(c.player()).push(new TeleportPadDestinationView(island, pad, 0)));
        layout.slot(DIRECTION_SLOT, (_, _) -> ItemStacks.item(Material.COMPASS, """
                <a>Change Teleport Direction
                <7>Change the direction you are
                <7>facing when you teleport to this
                <7>Teleport Pad!

                <7>Current Direction:
                <a>%s

                <e>Click to change!""".formatted(pad.direction().label())), (_, c) ->
                ViewNavigator.get(c.player()).push(new TeleportPadDirectionView(island, pad)));
        layout.slot(ICON_SLOT, (_, _) -> ItemStacks.item(Material.END_PORTAL_FRAME, """
                <a>Change Color
                <7>Click to set the Teleport Pad's
                <7>color!

                <7>Current Color:
                %s

                <e>Click to change!""".formatted(pad.iconType().label())), (_, c) ->
                ViewNavigator.get(c.player()).push(new TeleportPadIconView(island, pad, 0)));
        layout.slot(NAME_SLOT, (_, _) -> ItemStacks.item(Material.NAME_TAG, """
                <a>Rename Pad
                <7>Change its name to something
                <7>fancy!

                <7>Name:
                %s

                <e>Click to choose name!""".formatted(pad.name().isEmpty() ? "<c>None" : pad.displayName())), (_, c) ->
                rename((SkyBlockPlayer) c.player()));
        layout.slot(PICKUP_SLOT, (_, _) -> ItemStacks.item(Material.BEDROCK, """
                <a>Pickup Teleport Pad
                <e>Click to pickup!"""), (_, c) -> pickUp((SkyBlockPlayer) c.player()));
        Components.close(layout, CLOSE_SLOT);
    }

    private net.minestom.server.item.ItemStack.Builder destinationItem() {
        TeleportPad target = TeleportPads.of(island).byKey(pad.destination());
        String current = target == null ? "<c>None" : target.displayName();
        return ItemStacks.item(Material.ENDER_PEARL, """
                <a>Change Destination
                <7>Click to set the Teleport Pad's
                <7>destination!

                <7>Current Destination:
                %s

                <e>Click to change!""".formatted(current));
    }

    private void rename(SkyBlockPlayer player) {
        player.closeInventory();
        new HypixelSignGUI(player).open(new String[]{"Enter pad name", "(max " + TeleportPad.MAX_NAME_LENGTH + " letters)"})
                .thenAccept(input -> MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    if (!player.isOnline()) return;
                    pad.name(input == null ? "" : input);
                    TeleportPads.save(island);
                    player.openView(new TeleportPadView(island, pad));
                }));
    }

    private void pickUp(SkyBlockPlayer player) {
        TeleportPads.of(island).remove(pad.x(), pad.y(), pad.z());
        TeleportPads.save(island);
        island.getIslandInstance().setBlock(pad.x(), pad.y(), pad.z(), Block.AIR);
        player.closeInventory();
        player.addAndUpdateItem(new SkyBlockItem(ItemType.TELEPORT_PAD));
        player.sendMessage("<a>You picked up the Teleport Pad!");
    }
}
