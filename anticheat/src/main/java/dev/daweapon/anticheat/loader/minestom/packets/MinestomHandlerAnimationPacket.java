package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.packet.client.play.ClientAnimationPacket;
import dev.daweapon.anticheat.event.packet.AnimationPacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;

import java.util.UUID;

// Converts Minestom's client animation packet into an AnimationPacket.
public class MinestomHandlerAnimationPacket
        extends LoaderPacketHandler<ClientAnimationPacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, ClientAnimationPacket packet) {
        AnimationPacket.Hand hand = packet.hand() == PlayerHand.MAIN ?
                AnimationPacket.Hand.MAIN_HAND : AnimationPacket.Hand.OFF_HAND;

        return new AnimationPacket(uuid, hand);
    }

    @Override
    public ClientAnimationPacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return null;
    }

    @Override
    public Class<ClientAnimationPacket> getHandledPacketClass() {
        return ClientAnimationPacket.class;
    }
}
