package dev.daweapon.type.catacombs.map;

import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.map.DungeonMapCanvas;
import dev.daweapon.dungeons.catacombs.map.MapRoomView;
import dev.daweapon.type.catacombs.engine.DungeonHost;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.event.actions.player.ActionPlayerChangeHypixelMenuDisplay;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.MapDataPacket;
import net.minestom.server.tag.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntPredicate;

/**
 * The Magical Map in hotbar slot 9: a filled map painted from the layout as rooms are discovered and cleared, with a
 * green pointer for the holder and blue pointers for teammates.
 */
public final class DungeonMapView {
    public static final int SLOT = 8;
    private static final int FIRST_MAP_ID = 40_000;
    private static final AtomicInteger NEXT_ID = new AtomicInteger(FIRST_MAP_ID);
    private static final Tag<Integer> MAP_TAG = Tag.Integer("catacombs_map");
    private static final int SELF_ICON = 1;
    private static final int TEAMMATE_ICON = 3;

    private final DungeonHost host;
    private final FloorLayout layout;
    private final DungeonMapCanvas canvas;
    private final IntPredicate secretsDone;
    private final int mapId = NEXT_ID.getAndIncrement();
    private final ItemStack item;
    private final Set<Player> painted = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
    private volatile byte[] pixels = new byte[0];

    public DungeonMapView(DungeonHost host, FloorLayout layout, IntPredicate secretsDone) {
        this.host = host;
        this.layout = layout;
        this.canvas = new DungeonMapCanvas(layout);
        this.secretsDone = secretsDone;
        this.item = ItemStacks.notEditable(ItemStacks.item(Material.FILLED_MAP, """
                        <b>Magical Map
                        <7>Shows the layout of the Dungeon as
                        <7>it is explored and completed."""))
                .set(DataComponents.MAP_ID, mapId)
                .set(MAP_TAG, mapId)
                .build();
    }

    public ItemStack item() {
        return item;
    }

    public void poll() {
        byte[] next = canvas.render(MapRoomView.views(layout, host.state().rooms(), secretsDone));
        boolean changed = !Arrays.equals(next, pixels);
        pixels = next;
        if (changed) painted.clear();
        List<SkyBlockPlayer> party = party();
        for (SkyBlockPlayer viewer : party) {
            holdMap(viewer);
            MapDataPacket.ColorContent colours = painted.add(viewer)
                    ? new MapDataPacket.ColorContent((byte) DungeonMapCanvas.SIZE, (byte) DungeonMapCanvas.SIZE, (byte) 0, (byte) 0, next)
                    : null;
            viewer.sendPacket(new MapDataPacket(mapId, (byte) 0, true, true, icons(viewer, party), colours));
        }
    }

    private void holdMap(SkyBlockPlayer player) {
        ItemStack held = player.getInventory().getItemStack(SLOT);
        Integer id = held.getTag(MAP_TAG);
        if (id == null || id != mapId) player.getInventory().setItemStack(SLOT, item);
    }

    private List<MapDataPacket.Icon> icons(SkyBlockPlayer viewer, List<SkyBlockPlayer> party) {
        List<MapDataPacket.Icon> icons = new ArrayList<>();
        for (SkyBlockPlayer member : party) {
            if (member != viewer && host.alivePlayers().stream().noneMatch(alive -> alive == member)) continue;
            icons.add(new MapDataPacket.Icon(member == viewer ? SELF_ICON : TEAMMATE_ICON,
                    coordinate(canvas.pixelX(member.getPosition().x())),
                    coordinate(canvas.pixelZ(member.getPosition().z())),
                    direction(member.getPosition().yaw()), null));
        }
        return icons;
    }

    static byte coordinate(double pixel) {
        return (byte) Math.max(-128, Math.min(127, Math.round(pixel * 2 - DungeonMapCanvas.SIZE)));
    }

    static byte direction(float yaw) {
        return (byte) (((int) ((yaw + (yaw < 0 ? -8.0 : 8.0)) * 16.0 / 360.0)) & 15);
    }

    public boolean holds(ItemStack stack) {
        Integer id = stack == null ? null : stack.getTag(MAP_TAG);
        return id != null && id == mapId;
    }

    public void dispose() {
        for (SkyBlockPlayer player : party()) {
            if (holds(player.getInventory().getItemStack(SLOT))) {
                player.getInventory().setItemStack(SLOT, ItemStack.AIR);
                ActionPlayerChangeHypixelMenuDisplay.setMainMenu(player);
            }
        }
    }

    private List<SkyBlockPlayer> party() {
        return host.instance().getPlayers().stream()
                .filter(Player::isOnline)
                .filter(SkyBlockPlayer.class::isInstance)
                .map(SkyBlockPlayer.class::cast)
                .toList();
    }
}
