package dev.daweapon.anticheat.event.packet;

import lombok.Getter;
import dev.daweapon.anticheat.engine.AnticheatPlayer;

import java.util.UUID;

@Getter
// Base class for all loader-agnostic packet representations, resolving the sending AnticheatPlayer.
public abstract class AnticheatPacket {
    private final AnticheatPlayer player;

    public AnticheatPacket(UUID uuid) {
        this.player = AnticheatPlayer.players.get(uuid);
    }

    public AnticheatPacket(AnticheatPlayer player) {
        this.player = player;
    }
}
