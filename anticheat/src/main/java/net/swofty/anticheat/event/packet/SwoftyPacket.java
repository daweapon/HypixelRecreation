package net.swofty.anticheat.event.packet;

import lombok.Getter;
import net.swofty.anticheat.engine.SwoftyPlayer;

import java.util.UUID;

@Getter
// Base class for all loader-agnostic packet representations, resolving the sending SwoftyPlayer.
public abstract class SwoftyPacket {
    private final SwoftyPlayer player;

    public SwoftyPacket(UUID uuid) {
        this.player = SwoftyPlayer.players.get(uuid);
    }

    public SwoftyPacket(SwoftyPlayer player) {
        this.player = player;
    }
}
