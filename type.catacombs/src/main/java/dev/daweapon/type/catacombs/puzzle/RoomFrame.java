package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.RoomBlock;
import dev.daweapon.dungeons.catacombs.puzzle.RoomSpot;
import dev.daweapon.type.catacombs.arena.RoomFeatures;
import dev.daweapon.type.catacombs.arena.RoomMechanisms;
import net.minestom.server.coordinate.Pos;

/** Turns a puzzle room's template-frame positions into world positions for the room as the layout placed it. */
public record RoomFrame(PlacedRoom room) {
    public int[] block(RoomBlock local) {
        return RoomMechanisms.place(room, new int[]{local.x(), local.y(), local.z()});
    }

    public Pos spot(RoomSpot local) {
        RoomFeatures.Spot placed = RoomFeatures.place(room, new RoomFeatures.Spot(local.x(), local.y(), local.z(), local.yaw()));
        return new Pos(placed.x(), placed.y(), placed.z(), placed.yaw(), 0);
    }

    public Pos centre(RoomBlock local) {
        int[] at = block(local);
        return new Pos(at[0] + 0.5, at[1], at[2] + 0.5);
    }

    public static long key(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    public static long key(int[] at) {
        return key(at[0], at[1], at[2]);
    }
}
