package dev.daweapon.anticheat.event.packet;

import lombok.Getter;
import dev.daweapon.anticheat.engine.AnticheatPlayer;
import org.jetbrains.annotations.NotNull;

@Getter
// Loader-agnostic representation of a client's position-only movement packet.
public class PositionPacket extends AnticheatPacket {
    private final double x;
    private final double y;
    private final double z;
    private final boolean onGround;

    public PositionPacket(@NotNull AnticheatPlayer player, double x, double y, double z, boolean onGround) {
        super(player);
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
    }
}
