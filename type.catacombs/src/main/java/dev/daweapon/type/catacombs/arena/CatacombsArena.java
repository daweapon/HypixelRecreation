package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.FloorLayoutGenerator;
import dev.daweapon.dungeons.catacombs.layout.MobSpawnPoint;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.SharedInstance;

import java.util.Optional;

/** One run's own dungeon on a private instance: real rooms where the layout picked one, placeholder boxes elsewhere. */
public final class CatacombsArena {
    private static final Key FULLBRIGHT_DIMENSION = Key.key("skyblock:island");

    private final InstanceContainer container;
    private final SharedInstance instance;
    private final FloorLayout layout;
    private final CatacombsArenaBlocks blocks;

    private CatacombsArena(InstanceContainer container, SharedInstance instance, FloorLayout layout, CatacombsArenaBlocks blocks) {
        this.container = container;
        this.instance = instance;
        this.layout = layout;
        this.blocks = blocks;
    }

    public static CatacombsArena create(CatacombsFloor floor, CatacombsMode mode, long seed) {
        FloorLayout layout = FloorLayoutGenerator.generate(floor, mode, seed, CatacombsTemplates.get());
        CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, RoomStructures.shared(), true);
        InstanceManager manager = MinecraftServer.getInstanceManager();
        InstanceContainer container = manager.createInstanceContainer(
                MinecraftServer.getDimensionTypeRegistry().getKey(FULLBRIGHT_DIMENSION));
        container.setGenerator(new CatacombsArenaGenerator(blocks));
        return new CatacombsArena(container, manager.createSharedInstance(container), layout, blocks);
    }

    public SharedInstance instance() {
        return instance;
    }

    public FloorLayout layout() {
        return layout;
    }

    public CatacombsBlockPlan plan() {
        return blocks.plan();
    }

    public CatacombsArenaBlocks blocks() {
        return blocks;
    }

    /** Where Mort stands in this run's Entrance room, facing into it, when the room has a captured spot for him. */
    public Optional<Pos> mortSpot() {
        return RoomFeatures.shared().mort(layout.startRoom())
                .map(spot -> new Pos(spot.x(), spot.y(), spot.z(), spot.yaw(), 0));
    }

    public Optional<BossArena> bossArena() {
        return blocks.bossArena();
    }

    public Pos entranceSpawn() {
        return roomCentre(layout.startRoom());
    }

    public Pos roomCentre(PlacedRoom room) {
        return blocks.standPoint(room);
    }

    public Pos mobSpawn(PlacedRoom room, MobSpawnPoint spawn) {
        CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(room.origin());
        CellGeometry.PlanarPoint local = CellGeometry.rotateLocal(spawn.x(), spawn.z(), room.shape(), room.rotation());
        return new Pos(origin.x() + local.x(), origin.y() + spawn.y(), origin.z() + local.z());
    }

    public void destroy() {
        InstanceManager manager = MinecraftServer.getInstanceManager();
        manager.unregisterInstance(instance);
        manager.unregisterInstance(container);
    }
}
