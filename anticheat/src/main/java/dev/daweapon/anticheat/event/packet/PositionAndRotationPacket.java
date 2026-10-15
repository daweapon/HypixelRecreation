package dev.daweapon.anticheat.event.packet;

import lombok.Getter;
import dev.daweapon.anticheat.engine.AnticheatPlayer;
import dev.daweapon.anticheat.math.Pos;

@Getter
// Loader-agnostic representation of a client's combined position+rotation movement packet.
public class PositionAndRotationPacket extends AnticheatPacket {
    private final Pos pos;
    private final boolean onGround;

    public PositionAndRotationPacket(AnticheatPlayer player, Pos pos, boolean onGround) {
        super(player);
        this.pos = pos;
        this.onGround = onGround;
    }

}
