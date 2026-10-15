package dev.daweapon.type.generic.packet.packets.client;

import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientNameItemPacket;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
import dev.daweapon.type.generic.gui.HypixelAnvilGUI;
import dev.daweapon.type.generic.packet.HypixelPacketClientListener;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.Map;

public class PacketListenerNameItem extends HypixelPacketClientListener {
    @Override
    public Class<? extends ClientPacket> getPacket() {
        return ClientNameItemPacket.class;
    }

    @Override
    public void run(PlayerPacketEvent event, ClientPacket packet, HypixelPlayer player) {
        if (HypixelAnvilGUI.anvilGUIs.containsKey(player)) {
            HypixelAnvilGUI.anvilGUIs.put(player, Map.entry(((ClientNameItemPacket) packet).itemName(), HypixelAnvilGUI.anvilGUIs.get(player).getValue()));

            player.sendPacket(new WindowPropertyPacket(player.getOpenInventory().getWindowId(), (short) 0, (short) 0));
        }
    }

    @Override
    public boolean overrideMinestomProcessing() {
        return true;
    }
}
