package dev.daweapon.anticheat.loader.minestom.packets;

import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.event.packet.WindowClickPacket;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;

import java.util.UUID;

// Converts Minestom's client click-window packet into a WindowClickPacket.
public class MinestomHandlerWindowClickPacket
        extends LoaderPacketHandler<ClientClickWindowPacket> {

    @Override
    public AnticheatPacket buildAnticheatPacket(UUID uuid, ClientClickWindowPacket packet) {
        WindowClickPacket.ClickType clickType = mapClickType(packet.clickType().ordinal());

        return new WindowClickPacket(
                uuid,
                packet.windowId(),
                packet.stateId(),
                packet.slot(),
                packet.button(),
                clickType
        );
    }

    @Override
    public ClientClickWindowPacket buildLoaderPacket(UUID uuid, AnticheatPacket packet) {
        return null;
    }

    @Override
    public Class<ClientClickWindowPacket> getHandledPacketClass() {
        return ClientClickWindowPacket.class;
    }

    private WindowClickPacket.ClickType mapClickType(int type) {
        return switch (type) {
            case 0 -> WindowClickPacket.ClickType.PICKUP;
            case 1 -> WindowClickPacket.ClickType.QUICK_MOVE;
            case 2 -> WindowClickPacket.ClickType.SWAP;
            case 3 -> WindowClickPacket.ClickType.CLONE;
            case 4 -> WindowClickPacket.ClickType.THROW;
            case 5 -> WindowClickPacket.ClickType.QUICK_CRAFT;
            case 6 -> WindowClickPacket.ClickType.PICKUP_ALL;
            default -> WindowClickPacket.ClickType.PICKUP;
        };
    }
}
