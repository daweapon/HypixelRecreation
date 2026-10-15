package dev.daweapon.anticheat.loader;

import dev.daweapon.anticheat.event.packet.AnticheatPacket;
import dev.daweapon.anticheat.loader.managers.AnticheatSchedulerManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Platform integration point the anticheat uses to send packets/messages and dispatch loader-specific packet handlers.
public abstract class Loader {
    private final Map<String, LoaderPacketHandler> packetHandlerMap = new HashMap<>();

    public abstract AnticheatSchedulerManager getSchedulerManager();
    public abstract List<UUID> getOnlinePlayers();
    public abstract void onInitialize();
    public abstract void sendPacket(UUID uuid, AnticheatPacket packet);
    public abstract void sendMessage(UUID uuid, String message);

    public void registerPacketHandler(Class<? extends AnticheatPacket> clazz,
                                      LoaderPacketHandler packetHandler) {
        packetHandlerMap.put(clazz.getSimpleName(), packetHandler);
    }

    public @Nullable LoaderPacketHandler getPacketHandler(Class clazz) {
        for (Map.Entry<String, LoaderPacketHandler> entry : packetHandlerMap.entrySet()) {
            if (entry.getValue().getHandledPacketClass().equals(clazz)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public @Nullable LoaderPacketHandler getPacketHandler(AnticheatPacket packet) {
        return packetHandlerMap.get(packet.getClass().getSimpleName());
    }
}
