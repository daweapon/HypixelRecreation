package dev.daweapon.anticheat.engine;

import dev.daweapon.anticheat.event.AntiCheatListener;
import dev.daweapon.anticheat.event.ListenerMethod;
import dev.daweapon.anticheat.event.events.AnticheatPacketEvent;
import dev.daweapon.anticheat.event.packet.PingResponsePacket;
import dev.daweapon.anticheat.event.packet.AnticheatPacket;

// Listens for ping response packets and relays them to the player for latency calculation.
public class PingEvents extends AntiCheatListener {
    @ListenerMethod
    public void onPacketReceive(AnticheatPacketEvent event) {
        AnticheatPacket packet = event.packet();
        if (packet instanceof PingResponsePacket pingResponsePacket) {
            AnticheatPlayer player = pingResponsePacket.getPlayer();
            if (player != null) player.handlePingResponse(pingResponsePacket.getRequestId());
        }
    }
}
