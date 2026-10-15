package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.server.common.PingPacket;
import dev.daweapon.anticheat.engine.AnticheatPlayer;
import dev.daweapon.anticheat.event.packet.RequestPingPacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;

import java.util.UUID;

// Converts between Minestom's ping packet and a RequestPingPacket for anticheat latency measurement.
public class MinestomHandlerPingRequestPacket
        extends LoaderPacketHandler<PingPacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, PingPacket packet) {
        return new RequestPingPacket(
                AnticheatPlayer.players.get(uuid),
                packet.id()
        );
    }

    @Override
    public PingPacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return new PingPacket(
                ((RequestPingPacket) packet).getRequestId()
        );
    }

    @Override
    public Class<PingPacket> getHandledPacketClass() {
        return PingPacket.class;
    }
}
