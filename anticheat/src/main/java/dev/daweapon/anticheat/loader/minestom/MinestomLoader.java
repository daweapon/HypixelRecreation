package dev.daweapon.anticheat.loader.minestom;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientPongPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import dev.daweapon.anticheat.event.AnticheatEventHandler;
import dev.daweapon.anticheat.event.events.AnticheatPacketEvent;
import dev.daweapon.anticheat.event.packet.*;
import dev.daweapon.anticheat.loader.Loader;
import dev.daweapon.anticheat.loader.LoaderPacketHandler;
import dev.daweapon.anticheat.loader.managers.AnticheatSchedulerManager;
import dev.daweapon.anticheat.loader.minestom.packets.*;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Minestom-backed Loader implementation that registers packet handlers and bridges Minestom events to AnticheatPacket events.
public class MinestomLoader extends Loader {
    @Override
    public AnticheatSchedulerManager getSchedulerManager() {
        return new MinestomSchedulerManager();
    }

    @Override
    public List<UUID> getOnlinePlayers() {
        List<UUID> players = new ArrayList<>();
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> players.add(player.getUuid()));
        return players;
    }

    @Override
    public void onInitialize() {
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        registerPacketHandler(PositionAndRotationPacket.class,
                new MinestomHandlerPositionAndRotationPacket());
        registerPacketHandler(PositionPacket.class,
                new MinestomHandlerPositionPacket());
        registerPacketHandler(RotationPacket.class,
                new MinestomHandlerRotationPacket());
        registerPacketHandler(RequestPingPacket.class,
                new MinestomHandlerPingRequestPacket());
        registerPacketHandler(PingResponsePacket.class,
                new MinestomHandlerPingResponsePacket());
        registerPacketHandler(EntityActionPacket.class,
                new MinestomHandlerEntityActionPacket());
        registerPacketHandler(AbilitiesPacket.class,
                new MinestomHandlerAbilitiesPacket());
        registerPacketHandler(HeldItemChangePacket.class,
                new MinestomHandlerHeldItemChangePacket());
        registerPacketHandler(BlockDigPacket.class,
                new MinestomHandlerBlockDigPacket());
        registerPacketHandler(BlockPlacePacket.class,
                new MinestomHandlerBlockPlacePacket());
        registerPacketHandler(UseEntityPacket.class,
                new MinestomHandlerUseEntityPacket());
        registerPacketHandler(WindowClickPacket.class,
                new MinestomHandlerWindowClickPacket());
        registerPacketHandler(AnimationPacket.class,
                new MinestomHandlerAnimationPacket());
        registerPacketHandler(SteerVehiclePacket.class,
                new MinestomHandlerSteerVehiclePacket());

        globalEventHandler.addListener(PlayerPacketOutEvent.class, (event) -> {
            ServerPacket packet = event.getPacket();
            LoaderPacketHandler handler = getPacketHandler(packet.getClass());
            if (handler == null) return;

            AnticheatPacket anticheatPacket = handler.buildAnticheatPacket(event.getPlayer().getUuid(), packet);
            if (anticheatPacket == null) return;

            AnticheatEventHandler.callEvent(new AnticheatPacketEvent(anticheatPacket));
        });

        MinecraftServer.getPacketListenerManager().setPlayListener(
                ClientPongPacket.class, (packet, player) -> {
                    LoaderPacketHandler handler = getPacketHandler(ClientPongPacket.class);
                    AnticheatPacket anticheatPacket = handler.buildAnticheatPacket(player.getUuid(), packet);
                    if (anticheatPacket == null) return;

                    AnticheatEventHandler.callEvent(new AnticheatPacketEvent(anticheatPacket));
                });

        globalEventHandler.addListener(PlayerPacketEvent.class, (event) -> {
            ClientPacket packet = event.getPacket();
            LoaderPacketHandler handler = getPacketHandler(packet.getClass());
            if (handler == null) return;

            AnticheatPacket anticheatPacket = handler.buildAnticheatPacket(event.getPlayer().getUuid(), packet);
            if (anticheatPacket == null) return;

            AnticheatEventHandler.callEvent(new AnticheatPacketEvent(anticheatPacket));
        });
    }

    @Override
    public void sendPacket(UUID uuid, AnticheatPacket packet) {
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
        if (player == null) return;

        try {
            player.sendPacket((SendablePacket) getPacketHandler(packet).buildLoaderPacket(uuid, packet));
        } catch (Exception e) {
            Logger.error(e, "Error when attempting to send packet {} to player {}",
                    packet.getClass().getSimpleName(), uuid);
        }
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
        if (player == null) return;

        player.sendMessage(message);
    }
}
