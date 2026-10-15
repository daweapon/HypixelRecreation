package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import dev.daweapon.anticheat.engine.AnticheatPlayer;
import dev.daweapon.anticheat.event.packet.PositionPacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;

import java.util.UUID;

// Converts between Minestom's position-only packet and a PositionPacket.
public class MinestomHandlerPositionPacket
        extends LoaderPacketHandler<ClientPlayerPositionPacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, ClientPlayerPositionPacket packet) {
        return new PositionPacket(
                AnticheatPlayer.players.get(uuid),
                packet.position().x(),
                packet.position().y(),
                packet.position().z(),
                packet.onGround()
        );
    }

    @Override
    public ClientPlayerPositionPacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return new ClientPlayerPositionPacket(
                new net.minestom.server.coordinate.Pos(
                        ((PositionPacket) packet).getX(),
                        ((PositionPacket) packet).getY(),
                        ((PositionPacket) packet).getZ()
                ),
                (byte) (((PositionPacket) packet).isOnGround() ? 1 : 0)
        );
    }

    @Override
    public Class<ClientPlayerPositionPacket> getHandledPacketClass() {
        return ClientPlayerPositionPacket.class;
    }
}
