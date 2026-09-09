package net.swofty.anticheat.engine;

import net.swofty.anticheat.event.AntiCheatListener;
import net.swofty.anticheat.event.ListenerMethod;
import net.swofty.anticheat.event.events.AnticheatPacketEvent;
import net.swofty.anticheat.event.packet.PingResponsePacket;
import net.swofty.anticheat.event.packet.SwoftyPacket;

// Listens for ping response packets and relays them to the player for latency calculation.
public class PingEvents extends AntiCheatListener {
    @ListenerMethod
    public void onPacketReceive(AnticheatPacketEvent event) {
        SwoftyPacket packet = event.packet();
        if (packet instanceof PingResponsePacket pingResponsePacket) {
            SwoftyPlayer player = pingResponsePacket.getPlayer();
            if (player != null) player.handlePingResponse(pingResponsePacket.getRequestId());
        }
    }
}
