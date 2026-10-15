package dev.daweapon.anticheat.event.packet;

import lombok.Getter;
import dev.daweapon.anticheat.math.Pos;

import java.util.UUID;

@Getter
// Loader-agnostic representation of a client's block-dig (start/finish/cancel digging) packet.
public class BlockDigPacket extends AnticheatPacket {
    private final Status status;
    private final Pos position;
    private final Direction direction;
    private final int sequence;

    public BlockDigPacket(UUID uuid, Status status, Pos position, Direction direction, int sequence) {
        super(uuid);
        this.status = status;
        this.position = position;
        this.direction = direction;
        this.sequence = sequence;
    }

    public enum Status {
        STARTED_DIGGING,
        CANCELLED_DIGGING,
        FINISHED_DIGGING,
        DROP_ITEM_STACK,
        DROP_ITEM,
        UPDATE_HELD_ITEM,
        SWAP_ITEM_IN_HAND,
        STAB
    }

    public enum Direction {
        DOWN, UP, NORTH, SOUTH, WEST, EAST
    }
}
