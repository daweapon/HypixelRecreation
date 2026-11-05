package dev.daweapon.type.catacombs.secret;

import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.SecretAnchor;

import java.util.ArrayList;
import java.util.List;

/** Where every room's secret anchors land in the world once the room is rotated and placed. */
public final class SecretSpots {
    private SecretSpots() {
    }

    public static List<SecretSpot> of(FloorLayout layout) {
        List<SecretSpot> spots = new ArrayList<>();
        for (PlacedRoom room : layout.rooms()) {
            List<SecretAnchor> anchors = room.template().secretAnchors();
            for (int index = 0; index < anchors.size(); index++) spots.add(spot(room, index, anchors.get(index)));
        }
        return List.copyOf(spots);
    }

    private static SecretSpot spot(PlacedRoom room, int index, SecretAnchor anchor) {
        CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(room.origin());
        CellGeometry.PlanarPoint local = CellGeometry.rotateLocal(anchor.x() + 0.5, anchor.z() + 0.5, room.shape(), room.rotation());
        return new SecretSpot(room.index(), index, anchor.type(), origin.x() + (int) Math.floor(local.x()),
                origin.y() + anchor.y(), origin.z() + (int) Math.floor(local.z()));
    }
}
