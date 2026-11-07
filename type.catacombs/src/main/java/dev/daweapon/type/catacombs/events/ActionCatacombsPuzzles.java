package dev.daweapon.type.catacombs.events;

import dev.daweapon.type.catacombs.engine.DungeonEngine;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.ArrowHitBlockEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.tag.Tag;

/** Walking, clicking puzzle NPCs and shooting puzzle blocks inside a run's puzzle rooms. */
public class ActionCatacombsPuzzles implements HypixelEventClass {
    private static final Tag<Long> LAST_VOLLEY = Tag.Long("catacombs_puzzle_volley");

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onMove(PlayerMoveEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        DungeonEngine engine = ActionCatacombsSecrets.activeEngine(player);
        if (engine != null) engine.moved(player, event.getNewPosition(), event.isOnGround());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onInteractEntity(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN || !(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        DungeonEngine engine = ActionCatacombsSecrets.activeEngine(player);
        if (engine != null) engine.interactEntity(player, event.getTarget());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onArrowHit(ArrowHitBlockEvent event) {
        if (!(event.getShooter() instanceof SkyBlockPlayer player)) return;
        DungeonEngine engine = ActionCatacombsSecrets.activeEngine(player);
        if (engine == null) return;
        long volley = volley(event.getArrow());
        if (Long.valueOf(volley).equals(player.getTag(LAST_VOLLEY))) return;
        Point at = event.getHitPosition();
        if (engine.arrowHit(player, at.blockX(), at.blockY(), at.blockZ())) player.setTag(LAST_VOLLEY, volley);
    }

    static long volley(Entity arrow) {
        return arrow.getInstance().getWorldAge() - arrow.getAliveTicks();
    }
}
