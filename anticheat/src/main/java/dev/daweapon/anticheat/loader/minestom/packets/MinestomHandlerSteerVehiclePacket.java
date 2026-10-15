package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.client.play.ClientInputPacket;
import dev.daweapon.anticheat.event.packet.SteerVehiclePacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;

import java.util.UUID;

// Converts Minestom's client input packet into a SteerVehiclePacket.
public class MinestomHandlerSteerVehiclePacket
        extends LoaderPacketHandler<ClientInputPacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, ClientInputPacket packet) {

        float sideways = 0f;
        if (packet.left()) sideways -= 1f;
        if (packet.right()) sideways += 1f;

        float forward = 0f;
        if (packet.forward()) forward += 1f;
        if (packet.backward()) forward -= 1f;

        boolean jump = packet.jump();
        boolean unmount = packet.shift();

        return new SteerVehiclePacket(uuid, sideways, forward, jump, unmount);
    }

    @Override
    public ClientInputPacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return null;
    }

    @Override
    public Class<ClientInputPacket> getHandledPacketClass() {
        return ClientInputPacket.class;
    }
}