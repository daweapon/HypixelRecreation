package dev.daweapon.anticheat.event.packet;

import lombok.Getter;
import dev.daweapon.anticheat.engine.AnticheatPlayer;

@Getter
// Loader-agnostic representation of a client's rotation-only movement packet.
public class RotationPacket extends AnticheatPacket {
    private final float yaw;
    private final float pitch;
    private final boolean onGround;

    public RotationPacket(AnticheatPlayer player, float yaw, float pitch, boolean onGround) {
        super(player);
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

}
