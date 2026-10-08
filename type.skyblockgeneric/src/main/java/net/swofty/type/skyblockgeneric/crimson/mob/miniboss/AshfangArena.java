package net.swofty.type.skyblockgeneric.crimson.mob.miniboss;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public interface AshfangArena {
    void dropSoul(Pos at);

    boolean soulHit(SkyBlockPlayer player, Pos soul);
}
