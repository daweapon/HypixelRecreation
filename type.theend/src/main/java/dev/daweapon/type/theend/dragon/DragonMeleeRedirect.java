package dev.daweapon.type.theend.dragon;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.network.packet.client.play.ClientAttackPacket;

public final class DragonMeleeRedirect {
    public static final int CLIENT_PARTS = 8;

    private DragonMeleeRedirect() {
    }

    public static boolean isPart(int targetId, int dragonId) {
        int offset = targetId - dragonId;
        return offset >= 1 && offset <= CLIENT_PARTS;
    }

    public static void install() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPacketEvent.class, event -> {
            if (!(event.getPacket() instanceof ClientAttackPacket attack)) return;
            EnderDragonMob dragon = EnderDragonFight.dragon();
            if (dragon == null || dragon.isRemoved() || !isPart(attack.targetId(), dragon.getEntityId())) return;
            event.setCancelled(true);
            EventDispatcher.call(new EntityAttackEvent(event.getPlayer(), dragon));
        });
    }
}
