package net.swofty.anticheat.loader;

import lombok.Getter;
import lombok.Setter;
import net.swofty.anticheat.event.packet.SwoftyPacket;

import java.util.UUID;

@Getter
@Setter
// Converts between a platform-native packet type and the loader-agnostic SwoftyPacket representation.
public abstract class LoaderPacketHandler<T> {
    public abstract SwoftyPacket buildSwoftyPacket(UUID uuid, T packet);
    public abstract T buildLoaderPacket(UUID uuid, SwoftyPacket packet);
    public abstract Class<T> getHandledPacketClass();
}
