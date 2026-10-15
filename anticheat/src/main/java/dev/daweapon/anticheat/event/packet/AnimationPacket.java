package dev.daweapon.anticheat.event.packet;

import lombok.Getter;

import java.util.UUID;

@Getter
// Loader-agnostic representation of a client's hand-swing/animation packet.
public class AnimationPacket extends AnticheatPacket {
    private final Hand hand;

    public AnimationPacket(UUID uuid, Hand hand) {
        super(uuid);
        this.hand = hand;
    }

    public enum Hand {
        MAIN_HAND, OFF_HAND
    }
}
