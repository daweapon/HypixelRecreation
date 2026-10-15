package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import dev.daweapon.anticheat.event.packet.HeldItemChangePacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;

import java.util.UUID;

// Converts Minestom's client held-item-change packet into a HeldItemChangePacket.
public class MinestomHandlerHeldItemChangePacket
        extends LoaderPacketHandler<ClientHeldItemChangePacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, ClientHeldItemChangePacket packet) {
        return new HeldItemChangePacket(uuid, packet.slot());
    }

    @Override
    public ClientHeldItemChangePacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return null;
    }

    @Override
    public Class<ClientHeldItemChangePacket> getHandledPacketClass() {
        return ClientHeldItemChangePacket.class;
    }
}
