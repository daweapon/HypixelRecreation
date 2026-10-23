package dev.daweapon.type.hub.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerMoveEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.dante.DanteAccess;
import dev.daweapon.type.skyblockgeneric.dante.OfficialBuilding;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// While Dante is mayor, Goons turn away anyone without a Happy Mask or Dante's Talisman at the Official Buildings
public class ActionDanteGoonGuard implements HypixelEventClass {
    private static final long MESSAGE_COOLDOWN_MILLIS = 3000;
    private static final double PUSH_STRENGTH = 14;
    private static final Map<UUID, Long> LAST_MESSAGE = new ConcurrentHashMap<>();

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        if (!DanteAccess.securityActive()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        Pos target = event.getNewPosition();
        if (target.blockX() == player.getPosition().blockX() && target.blockZ() == player.getPosition().blockZ()
                && target.blockY() == player.getPosition().blockY()) return;

        OfficialBuilding destination = OfficialBuilding.at(target);
        if (destination == null || OfficialBuilding.at(player.getPosition()) != null) return;
        if (DanteAccess.canEnter(player)) return;

        event.setCancelled(true);
        Vec away = player.getPosition().sub(target).asVec().withY(0);
        if (away.length() > 0.0001) player.setVelocity(away.normalize().mul(PUSH_STRENGTH).withY(4));
        warn(player, destination);
    }

    private static void warn(SkyBlockPlayer player, OfficialBuilding building) {
        long now = System.currentTimeMillis();
        Long last = LAST_MESSAGE.get(player.getUuid());
        if (last != null && now - last < MESSAGE_COOLDOWN_MILLIS) return;
        LAST_MESSAGE.put(player.getUuid(), now);
        player.sendMessage("<e>[NPC] Goon<f>: Halt! The <e>" + building.getDisplayName()
                + " <f>is an Official Building. Show a <a>Happy Mask <f>or <6>Dante's Talisman <f>to enter.");
    }
}
