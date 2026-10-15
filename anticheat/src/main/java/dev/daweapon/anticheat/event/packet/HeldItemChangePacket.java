package dev.daweapon.anticheat.event.packet;

import lombok.Getter;

import java.util.UUID;

@Getter
// Loader-agnostic representation of a client's held-hotbar-slot-change packet.
public class HeldItemChangePacket extends AnticheatPacket {
    private final int slot;

    public HeldItemChangePacket(UUID uuid, int slot) {
        super(uuid);
        this.slot = slot;
    }
}
