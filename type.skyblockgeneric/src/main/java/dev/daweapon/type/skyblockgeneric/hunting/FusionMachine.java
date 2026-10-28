package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.hologram.HologramEntity;
import dev.daweapon.type.skyblockgeneric.gui.inventories.hunting.GUIAttributeFusion;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FusionMachine {
    private static final int MIN_X = -607;
    private static final int MAX_X = -605;
    private static final int MIN_Y = 115;
    private static final int MAX_Y = 117;
    private static final int MIN_Z = -16;
    private static final int MAX_Z = -14;

    private static final Pos LEFT_TUBE = new Pos(-604.5, 117.4, -17.5);
    private static final Pos RIGHT_TUBE = new Pos(-604.5, 117.4, -11.5);
    private static final Map<UUID, TubeDisplay> TUBES = new ConcurrentHashMap<>();

    private static boolean hologramSpawned;

    private FusionMachine() {
    }

    public static void register(Scheduler scheduler) {
        scheduler.scheduleTask(() -> {
            spawnHologram();
            removeOfflineTubes();
        }, TaskSchedule.seconds(1), TaskSchedule.seconds(5), ExecutionType.TICK_END);
    }

    public static boolean handle(SkyBlockPlayer player, Point block) {
        if (!onGalatea() || !contains(block)) return false;
        if (!HotfService.isUnlocked(player)) {
            player.sendMessage("<c>The Fusion Machine does not respond. Talk to Hina to get a Heart of the Forest first!");
            return true;
        }
        player.openView(new GUIAttributeFusion());
        return true;
    }

    static boolean contains(Point block) {
        return block.blockX() >= MIN_X && block.blockX() <= MAX_X && block.blockY() >= MIN_Y && block.blockY() <= MAX_Y
                && block.blockZ() >= MIN_Z && block.blockZ() <= MAX_Z;
    }

    private static boolean onGalatea() {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_GALATEA;
    }

    private static void spawnHologram() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (hologramSpawned || instance == null || !onGalatea()) return;
        hologramSpawned = true;
        new HologramEntity("<d><l>FUSION MACHINE").setInstance(instance, new Pos(-604.5, 116.75, -14.5));
        new HologramEntity("<e><l>Click to view!").setInstance(instance, new Pos(-604.5, 116.4, -14.5));
    }

    public static void showTubes(SkyBlockPlayer player, AttributeDefinition first, AttributeDefinition second) {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null || !onGalatea()) return;
        String signature = (first == null ? "" : first.id().toString()) + "|" + (second == null ? "" : second.id().toString());
        TubeDisplay current = TUBES.get(player.getUuid());
        if (current != null && current.signature().equals(signature)) return;
        clearTubes(player);
        if (first == null && second == null) return;
        List<Entity> entities = new ArrayList<>();
        if (first != null) entities.add(tubeShard(player, instance, first, LEFT_TUBE));
        if (second != null) entities.add(tubeShard(player, instance, second, RIGHT_TUBE));
        TUBES.put(player.getUuid(), new TubeDisplay(signature, entities));
    }

    public static void clearTubes(SkyBlockPlayer player) {
        TubeDisplay removed = TUBES.remove(player.getUuid());
        if (removed != null) removed.entities().forEach(Entity::remove);
    }

    private static Entity tubeShard(SkyBlockPlayer player, Instance instance, AttributeDefinition definition, Pos position) {
        Entity display = new Entity(EntityType.ITEM_DISPLAY);
        display.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(AttributeShardComponent.create(definition, 1).getItemStackBuilder().build());
            meta.setDisplayContext(ItemDisplayMeta.DisplayContext.HEAD);
            meta.setScale(new Vec(0.9, 0.9, 0.9));
            meta.setHasNoGravity(true);
        });
        display.setNoGravity(true);
        display.setAutoViewable(false);
        display.setInstance(instance, position);
        display.addViewer(player);
        return display;
    }

    private static void removeOfflineTubes() {
        TUBES.entrySet().removeIf(entry -> {
            boolean offline = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(entry.getKey()) == null;
            if (offline) entry.getValue().entities().forEach(Entity::remove);
            return offline;
        });
    }

    private record TubeDisplay(String signature, List<Entity> entities) {
    }
}
