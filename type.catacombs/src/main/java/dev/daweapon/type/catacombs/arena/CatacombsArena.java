package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.FloorLayoutGenerator;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.MobSpawnPoint;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.instance.block.Block;

/** One run's own placeholder dungeon: the generated layout stamped as a flat grid on a private instance. */
public final class CatacombsArena {
    private static final int STANDING_OFFSET = CatacombsBlockPlan.MARKER_RADIUS + 2;
    private static final Key FULLBRIGHT_DIMENSION = Key.key("skyblock:island");

    private final InstanceContainer container;
    private final SharedInstance instance;
    private final FloorLayout layout;
    private final CatacombsBlockPlan plan;

    private CatacombsArena(InstanceContainer container, SharedInstance instance, FloorLayout layout, CatacombsBlockPlan plan) {
        this.container = container;
        this.instance = instance;
        this.layout = layout;
        this.plan = plan;
    }

    public static CatacombsArena create(CatacombsFloor floor, CatacombsMode mode, long seed) {
        FloorLayout layout = FloorLayoutGenerator.generate(floor, mode, seed, CatacombsTemplates.get());
        CatacombsBlockPlan plan = new CatacombsBlockPlan(layout);
        InstanceManager manager = MinecraftServer.getInstanceManager();
        InstanceContainer container = manager.createInstanceContainer(
                MinecraftServer.getDimensionTypeRegistry().getKey(FULLBRIGHT_DIMENSION));
        container.setGenerator(new CatacombsPlaceholderGenerator(plan));
        return new CatacombsArena(container, manager.createSharedInstance(container), layout, plan);
    }

    public SharedInstance instance() {
        return instance;
    }

    public FloorLayout layout() {
        return layout;
    }

    public CatacombsBlockPlan plan() {
        return plan;
    }

    public Pos entranceSpawn() {
        return roomCentre(layout.startRoom());
    }

    public Pos roomCentre(PlacedRoom room) {
        CellGeometry.BlockPosition centre = CellGeometry.roomCenter(room.cells().getFirst());
        return new Pos(centre.x() + 0.5, centre.y() + 1, centre.z() + 0.5 + STANDING_OFFSET);
    }

    public Pos mobSpawn(PlacedRoom room, MobSpawnPoint spawn) {
        CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(room.origin());
        CellGeometry.PlanarPoint local = CellGeometry.rotateLocal(spawn.x(), spawn.z(), room.shape(), room.rotation());
        return new Pos(origin.x() + local.x(), origin.y() + spawn.y(), origin.z() + local.z());
    }

    public void fill(LayoutDoor door, Block block) {
        for (int[] at : DoorFrame.of(door).blocks()) instance.setBlock(at[0], at[1], at[2], block);
    }

    public void destroy() {
        InstanceManager manager = MinecraftServer.getInstanceManager();
        manager.unregisterInstance(instance);
        manager.unregisterInstance(container);
    }
}
