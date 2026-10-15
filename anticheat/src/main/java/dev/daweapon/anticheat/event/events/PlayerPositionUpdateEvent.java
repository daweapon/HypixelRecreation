package dev.daweapon.anticheat.event.events;

import lombok.Getter;
import dev.daweapon.anticheat.engine.PlayerTickInformation;
import dev.daweapon.anticheat.engine.AnticheatPlayer;

@Getter
// Event fired each time a player's tick advances, carrying the previous and current tick snapshots.
public class PlayerPositionUpdateEvent {
    private final AnticheatPlayer player;
    private final PlayerTickInformation previousTick;
    private final PlayerTickInformation currentTick;

    public PlayerPositionUpdateEvent(AnticheatPlayer player, PlayerTickInformation previousTick,
                                     PlayerTickInformation currentTick) {
        this.player = player;
        this.previousTick = previousTick;
        this.currentTick = currentTick;
    }
}
