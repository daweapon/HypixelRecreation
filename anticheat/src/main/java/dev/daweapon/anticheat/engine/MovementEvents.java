package dev.daweapon.anticheat.engine;

import dev.daweapon.anticheat.event.AntiCheatListener;
import dev.daweapon.anticheat.event.ListenerMethod;
import dev.daweapon.anticheat.event.events.AnticheatPacketEvent;
import dev.daweapon.anticheat.event.packet.IsOnGroundPacket;
import dev.daweapon.anticheat.event.packet.PositionAndRotationPacket;
import dev.daweapon.anticheat.event.packet.PositionPacket;
import dev.daweapon.anticheat.event.packet.RotationPacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.math.Pos;

// Listens for movement-related packets and forwards the resulting position/on-ground state to the player.
public class MovementEvents extends AntiCheatListener {
    @ListenerMethod
    public void onPacketReceive(AnticheatPacketEvent event) {
        AnticheatPacket packet = event.packet();
        AnticheatPlayer player = packet.getPlayer();

        Pos pos = player.getCurrentTick().getPos();
        boolean onGround = player.getCurrentTick().isOnGround();

        switch (packet) {
            case PositionPacket positionPacket -> {
                pos = new Pos(positionPacket.getX(),
                        positionPacket.getY(), positionPacket.getZ(),
                        pos.yaw(), pos.pitch());
                onGround = positionPacket.isOnGround();
            }
            case PositionAndRotationPacket positionAndRotationPacket -> {
                pos = positionAndRotationPacket.getPos();
                onGround = positionAndRotationPacket.isOnGround();
            }
            case IsOnGroundPacket isOnGroundPacket -> onGround = isOnGroundPacket.isOnGround();
            case RotationPacket rotationPacket ->
                    pos = new Pos(pos.x(), pos.y(), pos.z(), rotationPacket.getYaw(), rotationPacket.getPitch());
            default -> {
            }
        }

        player.processMovement(pos, onGround);
    }
}
