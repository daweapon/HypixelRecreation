package dev.daweapon.anticheat.event.packet;

import lombok.Getter;
import net.minestom.server.entity.PlayerHand;
import dev.daweapon.anticheat.math.Pos;

import java.util.UUID;

@Getter
// Loader-agnostic representation of a client's interact/attack-entity packet.
public class UseEntityPacket extends AnticheatPacket {
    private final int entityId;
    private final Pos targetPosition;
    private final PlayerHand hand;

    public UseEntityPacket(UUID uuid, int entityId, Pos targetPosition, PlayerHand hand) {
        super(uuid);
        this.entityId = entityId;
        this.targetPosition = targetPosition;
        this.hand = hand;
    }
}
