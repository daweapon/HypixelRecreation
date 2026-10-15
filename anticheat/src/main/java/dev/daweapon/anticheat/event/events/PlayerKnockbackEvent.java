package dev.daweapon.anticheat.event.events;

import lombok.Getter;
import dev.daweapon.anticheat.engine.AnticheatPlayer;
import dev.daweapon.anticheat.math.Vel;

@Getter
// Event fired when a player receives knockback velocity, used by velocity/knockback-related checks.
public class PlayerKnockbackEvent {
    private final AnticheatPlayer player;
    private final Vel knockbackVelocity;

    public PlayerKnockbackEvent(AnticheatPlayer player, Vel knockbackVelocity) {
        this.player = player;
        this.knockbackVelocity = knockbackVelocity;
    }
}
