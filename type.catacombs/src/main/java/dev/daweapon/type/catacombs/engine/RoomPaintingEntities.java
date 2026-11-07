package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.type.catacombs.arena.RoomPaintings;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.registry.RegistryKey;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Hangs the captured paintings in the run's real rooms. Paintings have no collision, so a painting over a secret passage
 * is walked through, as on Hypixel.
 */
final class RoomPaintingEntities {
    private final DungeonHost host;
    private final FloorLayout layout;
    private final RoomPaintings paintings;
    private final List<Entity> hung = new CopyOnWriteArrayList<>();

    RoomPaintingEntities(DungeonHost host, FloorLayout layout, RoomPaintings paintings) {
        this.host = host;
        this.layout = layout;
        this.paintings = paintings;
    }

    void start() {
        for (PlacedRoom room : layout.rooms()) {
            if (!host.arena().blocks().isReal(room)) continue;
            for (RoomPaintings.Painting painting : paintings.of(room)) hang(painting);
        }
    }

    private void hang(RoomPaintings.Painting painting) {
        Entity entity = new Entity(EntityType.PAINTING);
        entity.setNoGravity(true);
        entity.editEntityMeta(PaintingMeta.class, meta -> {
            meta.setDirection(painting.facing());
            meta.setVariant(RegistryKey.unsafeOf(painting.variant()));
        });
        entity.setInstance(host.instance(), new Pos(painting.x(), painting.y(), painting.z()));
        hung.add(entity);
    }

    void dispose() {
        hung.forEach(Entity::remove);
        hung.clear();
    }
}
