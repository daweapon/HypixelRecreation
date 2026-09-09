package net.swofty.anticheat.event.packet;

import lombok.Getter;

import java.util.UUID;

@Getter
// Loader-agnostic representation of a client's entity-action packet (sneak, sprint, elytra, horse jump, etc.).
public class EntityActionPacket extends SwoftyPacket {
    private final Action action;
    private final int jumpBoost;

    public EntityActionPacket(UUID uuid, Action action, int jumpBoost) {
        super(uuid);
        this.action = action;
        this.jumpBoost = jumpBoost;
    }

    public enum Action {
        START_SNEAKING,
        STOP_SNEAKING,
        LEAVE_BED,
        START_SPRINTING,
        STOP_SPRINTING,
        START_JUMP_WITH_HORSE,
        STOP_JUMP_WITH_HORSE,
        OPEN_HORSE_INVENTORY,
        START_FLYING_WITH_ELYTRA
    }
}
