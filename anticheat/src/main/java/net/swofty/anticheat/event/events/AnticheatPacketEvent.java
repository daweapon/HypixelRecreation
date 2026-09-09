package net.swofty.anticheat.event.events;

import net.swofty.anticheat.event.packet.SwoftyPacket;

// Event fired whenever an anticheat-relevant packet is received from a client.
public record AnticheatPacketEvent(SwoftyPacket packet) {

}
