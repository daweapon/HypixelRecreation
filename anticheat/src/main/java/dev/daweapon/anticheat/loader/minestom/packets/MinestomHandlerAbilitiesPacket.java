package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.client.play.ClientPlayerAbilitiesPacket;
import dev.daweapon.anticheat.event.packet.AbilitiesPacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;

import java.util.UUID;

// Converts Minestom's client player-abilities packet into an AbilitiesPacket.
public class MinestomHandlerAbilitiesPacket
        extends LoaderPacketHandler<ClientPlayerAbilitiesPacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, ClientPlayerAbilitiesPacket packet) {
        byte flags = packet.flags();
        boolean invulnerable = (flags & 0x01) != 0;
        boolean flying = (flags & 0x02) != 0;
        boolean allowFlight = (flags & 0x04) != 0;
        boolean creativeMode = (flags & 0x08) != 0;

        return new AbilitiesPacket(uuid, invulnerable, flying, allowFlight, creativeMode, 0.05f, 0.1f);
    }

    @Override
    public ClientPlayerAbilitiesPacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return null;
    }

    @Override
    public Class<ClientPlayerAbilitiesPacket> getHandledPacketClass() {
        return ClientPlayerAbilitiesPacket.class;
    }
}
