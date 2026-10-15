package dev.daweapon.anticheat.event.events;

import dev.daweapon.anticheat.event.packet.AnticheatPacket;

// Event fired whenever an anticheat-relevant packet is received from a client.
public record AnticheatPacketEvent(AnticheatPacket packet) {

}
