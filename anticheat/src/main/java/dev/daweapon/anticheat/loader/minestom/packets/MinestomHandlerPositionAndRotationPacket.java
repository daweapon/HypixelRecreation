package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket;
import dev.daweapon.anticheat.engine.AnticheatPlayer;
import dev.daweapon.anticheat.event.packet.PositionAndRotationPacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;
import dev.daweapon.anticheat.math.Pos;

import java.util.UUID;

// Converts between Minestom's combined position+rotation packet and a PositionAndRotationPacket.
public class MinestomHandlerPositionAndRotationPacket
        extends LoaderPacketHandler<ClientPlayerPositionAndRotationPacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, ClientPlayerPositionAndRotationPacket packet) {
        return new PositionAndRotationPacket(
                AnticheatPlayer.players.get(uuid),
                new Pos(packet.position().x(),
                        packet.position().y(),
                        packet.position().z(),
                        packet.position().yaw(),
                        packet.position().pitch()),
                packet.onGround()
        );
    }

    @Override
    public ClientPlayerPositionAndRotationPacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return new ClientPlayerPositionAndRotationPacket(
                new net.minestom.server.coordinate.Pos(
                        ((PositionAndRotationPacket) packet).getPos().x(),
                        ((PositionAndRotationPacket) packet).getPos().y(),
                        ((PositionAndRotationPacket) packet).getPos().z(),
                        ((PositionAndRotationPacket) packet).getPos().yaw(),
                        ((PositionAndRotationPacket) packet).getPos().pitch()
                ),
                (byte) (((PositionAndRotationPacket) packet).isOnGround() ? 1 : 0)
        );
    }

    @Override
    public Class<ClientPlayerPositionAndRotationPacket> getHandledPacketClass() {
        return ClientPlayerPositionAndRotationPacket.class;
    }
}
