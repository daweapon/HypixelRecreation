package net.swofty.anticheat.event.events;

import lombok.Getter;
import net.swofty.anticheat.engine.PlayerTickInformation;
import net.swofty.anticheat.engine.SwoftyPlayer;

@Getter
// Event fired each time a player's tick advances, carrying the previous and current tick snapshots.
public class PlayerPositionUpdateEvent {
    private final SwoftyPlayer player;
    private final PlayerTickInformation previousTick;
    private final PlayerTickInformation currentTick;

    public PlayerPositionUpdateEvent(SwoftyPlayer player, PlayerTickInformation previousTick,
                                     PlayerTickInformation currentTick) {
        this.player = player;
        this.previousTick = previousTick;
        this.currentTick = currentTick;
    }
}
