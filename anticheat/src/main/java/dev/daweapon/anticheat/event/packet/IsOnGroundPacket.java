package dev.daweapon.anticheat.event.packet;

import lombok.Getter;
import dev.daweapon.anticheat.engine.AnticheatPlayer;

@Getter
// Loader-agnostic representation of a client's on-ground-only movement packet.
public class IsOnGroundPacket extends AnticheatPacket {
    private final boolean onGround;

    public IsOnGroundPacket(AnticheatPlayer player, boolean onGround) {
        super(player);
        this.onGround = onGround;
    }

}
