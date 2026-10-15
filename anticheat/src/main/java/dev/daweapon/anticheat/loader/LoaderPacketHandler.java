package dev.daweapon.anticheat.loader;

import lombok.Getter;
import lombok.Setter;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;

import java.util.UUID;

@Getter
@Setter
// Converts between a platform-native packet type and the loader-agnostic AnticheatPacket representation.
public abstract class LoaderPacketHandler<T> {
    public abstract AnticheatPacket buildAnticheatPacket(UUID uuid, T packet);
    public abstract T buildLoaderPacket(UUID uuid, AnticheatPacket packet);
    public abstract Class<T> getHandledPacketClass();
}
