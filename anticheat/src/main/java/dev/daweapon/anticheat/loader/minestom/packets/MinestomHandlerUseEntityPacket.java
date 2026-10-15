package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.event.packet.UseEntityPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;
import dev.daweapon.anticheat.math.Pos;

import java.util.UUID;

// Converts Minestom's client interact-entity packet into a UseEntityPacket.
public class MinestomHandlerUseEntityPacket
        extends LoaderPacketHandler<ClientInteractEntityPacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, ClientInteractEntityPacket packet) {
        return new UseEntityPacket(
                uuid,
                packet.targetId(),
            Pos.fromVec(packet.location()),
            packet.hand()
        );
    }

    @Override
    public ClientInteractEntityPacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return null;
    }

    @Override
    public Class<ClientInteractEntityPacket> getHandledPacketClass() {
        return ClientInteractEntityPacket.class;
    }
}
