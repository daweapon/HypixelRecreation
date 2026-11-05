package dev.daweapon.type.catacombs.secret;

import dev.daweapon.dungeons.catacombs.layout.SecretType;
import net.minestom.server.coordinate.Pos;

/** One secret anchor of a placed room, at its world block. */
public record SecretSpot(int room, int anchor, SecretType type, int x, int y, int z) {
    public SecretLedger.Key key() {
        return new SecretLedger.Key(room, anchor);
    }

    public Pos centre() {
        return new Pos(x + 0.5, y, z + 0.5);
    }

    public boolean at(int blockX, int blockY, int blockZ) {
        return x == blockX && y == blockY && z == blockZ;
    }
}
