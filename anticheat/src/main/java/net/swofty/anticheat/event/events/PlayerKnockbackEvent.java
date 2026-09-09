package net.swofty.anticheat.event.events;

import lombok.Getter;
import net.swofty.anticheat.engine.SwoftyPlayer;
import net.swofty.anticheat.math.Vel;

@Getter
// Event fired when a player receives knockback velocity, used by velocity/knockback-related checks.
public class PlayerKnockbackEvent {
    private final SwoftyPlayer player;
    private final Vel knockbackVelocity;

    public PlayerKnockbackEvent(SwoftyPlayer player, Vel knockbackVelocity) {
        this.player = player;
        this.knockbackVelocity = knockbackVelocity;
    }
}
