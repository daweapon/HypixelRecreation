package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.client.common.ClientPongPacket;
import dev.daweapon.anticheat.engine.AnticheatPlayer;
import dev.daweapon.anticheat.event.packet.PingResponsePacket;
import dev.daweapon.anticheat.event.packet.RequestPingPacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;

import java.util.UUID;

// Converts between Minestom's client pong packet and a PingResponsePacket.
public class MinestomHandlerPingResponsePacket
        extends LoaderPacketHandler<ClientPongPacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, ClientPongPacket packet) {
        return new PingResponsePacket(
                AnticheatPlayer.players.get(uuid),
                packet.id()
        );
    }

    @Override
    public ClientPongPacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return new ClientPongPacket(
                ((RequestPingPacket) packet).getRequestId()
        );
    }

    @Override
    public Class<ClientPongPacket> getHandledPacketClass() {
        return ClientPongPacket.class;
    }
}
