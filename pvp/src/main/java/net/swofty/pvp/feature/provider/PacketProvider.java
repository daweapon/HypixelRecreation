package net.swofty.pvp.feature.provider;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Entity;
import net.minestom.server.network.packet.server.ServerPacket;
import net.swofty.pvp.feature.CombatFeature;

// Combat feature abstracting how packets are sent to viewers, so senders can be swapped out (e.g. for grouping).
public interface PacketProvider extends CombatFeature {
    PacketProvider DEFAULT = Viewable::sendPacketToViewersAndSelf;

    void sendPacket(Entity viewable, ServerPacket.Play packet);
}
