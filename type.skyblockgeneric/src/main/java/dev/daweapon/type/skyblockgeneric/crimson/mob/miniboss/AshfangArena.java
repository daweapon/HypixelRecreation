package dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public interface AshfangArena {
    void dropSoul(Pos at);

    boolean soulHit(SkyBlockPlayer player, Pos soul);
}
