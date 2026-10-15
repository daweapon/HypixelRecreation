package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import dev.daweapon.anticheat.event.packet.BlockPlacePacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;
import dev.daweapon.anticheat.math.Pos;

import java.util.UUID;

// Converts Minestom's client block-placement packet into a BlockPlacePacket.
public class MinestomHandlerBlockPlacePacket
        extends LoaderPacketHandler<ClientPlayerBlockPlacementPacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, ClientPlayerBlockPlacementPacket packet) {
        BlockPlacePacket.Hand hand = packet.hand() == PlayerHand.MAIN ?
                BlockPlacePacket.Hand.MAIN_HAND : BlockPlacePacket.Hand.OFF_HAND;
        BlockPlacePacket.Direction direction = mapDirection(packet.blockFace());
        Pos position = new Pos(
                packet.blockPosition().blockX(),
                packet.blockPosition().blockY(),
                packet.blockPosition().blockZ()
        );

        return new BlockPlacePacket(
                uuid, hand, position, direction,
                packet.cursorPositionX(), packet.cursorPositionY(), packet.cursorPositionZ(),
                packet.insideBlock(), packet.sequence()
        );
    }

    @Override
    public ClientPlayerBlockPlacementPacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return null;
    }

    @Override
    public Class<ClientPlayerBlockPlacementPacket> getHandledPacketClass() {
        return ClientPlayerBlockPlacementPacket.class;
    }

    private BlockPlacePacket.Direction mapDirection(net.minestom.server.instance.block.BlockFace face) {
        return switch (face) {
            case BOTTOM -> BlockPlacePacket.Direction.DOWN;
            case TOP -> BlockPlacePacket.Direction.UP;
            case NORTH -> BlockPlacePacket.Direction.NORTH;
            case SOUTH -> BlockPlacePacket.Direction.SOUTH;
            case WEST -> BlockPlacePacket.Direction.WEST;
            case EAST -> BlockPlacePacket.Direction.EAST;
        };
    }
}
