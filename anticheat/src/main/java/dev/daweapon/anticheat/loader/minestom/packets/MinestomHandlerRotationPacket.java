package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import dev.daweapon.anticheat.engine.AnticheatPlayer;
import dev.daweapon.anticheat.event.packet.RotationPacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;

import java.util.UUID;

// Converts between Minestom's rotation-only packet and a RotationPacket.
public class MinestomHandlerRotationPacket
        extends LoaderPacketHandler<ClientPlayerRotationPacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, ClientPlayerRotationPacket packet) {
        return new RotationPacket(
                AnticheatPlayer.players.get(uuid),
                packet.yaw(),
                packet.pitch(),
                packet.onGround()
        );
    }

    @Override
    public ClientPlayerRotationPacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return new ClientPlayerRotationPacket(
                ((RotationPacket) packet).getYaw(),
                ((RotationPacket) packet).getPitch(),
                (byte) (((RotationPacket) packet).isOnGround() ? 1 : 0)
        );
    }

    @Override
    public Class<ClientPlayerRotationPacket> getHandledPacketClass() {
        return ClientPlayerRotationPacket.class;
    }
}
