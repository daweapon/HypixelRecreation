package dev.daweapon.anticheat.event.events;

import lombok.Getter;
import dev.daweapon.anticheat.engine.AnticheatPlayer;
import dev.daweapon.anticheat.math.Pos;

import java.util.UUID;

@Getter
// Event fired when a player attacks another entity, used by combat-related checks (e.g. reach, killaura).
public class PlayerAttackEvent {
    private final AnticheatPlayer attacker;
    private final UUID targetUuid;
    private final Pos targetPosition;
    private final long timestamp;

    public PlayerAttackEvent(AnticheatPlayer attacker, UUID targetUuid, Pos targetPosition) {
        this.attacker = attacker;
        this.targetUuid = targetUuid;
        this.targetPosition = targetPosition;
        this.timestamp = System.currentTimeMillis();
    }
}
