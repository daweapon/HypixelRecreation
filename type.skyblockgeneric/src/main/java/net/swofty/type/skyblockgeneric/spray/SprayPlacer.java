package net.swofty.type.skyblockgeneric.spray;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.MapDataPacket;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prints the selected spray onto a block face as an invisible item frame holding the spray's map.
 * On a Private Island everyone sees it and it costs ink; in the Canvas Room only the sprayer sees it
 * and it is free.
 */
public final class SprayPlacer {
    private static final long COOLDOWN_MILLIS = 1_000;
    private static final int MAX_SPRAYS_PER_INSTANCE = 64;
    private static final Map<UUID, Long> LAST_SPRAY = new ConcurrentHashMap<>();
    private static final Map<Instance, Deque<SprayFrame>> SPRAYS = new ConcurrentHashMap<>();

    private SprayPlacer() {}

    public static boolean canSprayHere(SkyBlockPlayer player) {
        return HypixelConst.isIslandServer() || CanvasRoomPainter.isInCanvasRoom(player);
    }

    public static void spray(SkyBlockPlayer player, SkyBlockItem can, BlockVec position, BlockFace face) {
        if (!canSprayHere(player)) {
            player.sendMessage("<c>You can only spray on your Private Island or in Marco's Canvas Room!");
            return;
        }
        SprayCanData data = SprayCanData.of(can);
        Spray spray = data.selected().orElse(null);
        if (spray == null) {
            player.sendMessage("<c>Select a spray first by left-clicking your Spray Can!");
            return;
        }
        long now = System.currentTimeMillis();
        if (now - LAST_SPRAY.getOrDefault(player.getUuid(), 0L) < COOLDOWN_MILLIS) return;

        boolean free = CanvasRoomPainter.isInCanvasRoom(player);
        if (!free) {
            if (!data.canAfford(spray)) {
                player.sendMessage("<c>You don't have enough ink to spray this! Refill your Spray Can with dyes.");
                return;
            }
            data.spend(spray);
            data.writeTo(can);
            player.setItemInMainHand(can.getItemStack());
        }
        LAST_SPRAY.put(player.getUuid(), now);
        place(player, spray, data.getDirection(), position, face, free ? player.getUuid() : null);
    }

    private static void place(SkyBlockPlayer player, Spray spray, SprayDirection direction, BlockVec position,
                              BlockFace face, UUID onlyViewer) {
        Instance instance = player.getInstance();
        SPRAYS.keySet().removeIf(known -> !MinecraftServer.getInstanceManager().getInstances().contains(known));
        Deque<SprayFrame> sprays = SPRAYS.computeIfAbsent(instance, ignored -> new ArrayDeque<>());
        synchronized (sprays) {
            sprays.removeIf(existing -> {
                boolean replaced = existing.isRemoved() || existing.covers(position, face, onlyViewer);
                if (replaced) existing.remove();
                return replaced;
            });
            while (sprays.size() >= MAX_SPRAYS_PER_INSTANCE) sprays.pollFirst().remove();
            SprayFrame frame = new SprayFrame(SprayImages.packet(spray, direction), position, face, onlyViewer);
            if (onlyViewer != null) frame.updateViewableRule(viewer -> viewer.getUuid().equals(onlyViewer));
            frame.setInstance(instance, new Pos(position.blockX() + 0.5, position.blockY(), position.blockZ() + 0.5));
            sprays.addLast(frame);
        }
    }

    private static final class SprayFrame extends Entity {
        private final MapDataPacket mapData;
        private final BlockVec position;
        private final BlockFace face;
        private final UUID onlyViewer;

        private SprayFrame(MapDataPacket mapData, BlockVec position, BlockFace face, UUID onlyViewer) {
            super(EntityType.ITEM_FRAME);
            this.mapData = mapData;
            this.position = position;
            this.face = face;
            this.onlyViewer = onlyViewer;
            setNoGravity(true);
            editEntityMeta(ItemFrameMeta.class, meta -> {
                meta.setInvisible(true);
                meta.setDirection(face.toDirection());
                meta.setItem(ItemStack.builder(Material.FILLED_MAP).set(DataComponents.MAP_ID, mapData.mapId()).build());
            });
        }

        private boolean covers(BlockVec otherPosition, BlockFace otherFace, UUID otherViewer) {
            return position.equals(otherPosition) && face == otherFace
                    && (onlyViewer == null ? otherViewer == null : onlyViewer.equals(otherViewer));
        }

        @Override
        public void updateNewViewer(@NotNull Player player) {
            player.sendPacket(mapData);
            super.updateNewViewer(player);
        }
    }
}
