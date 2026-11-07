package dev.daweapon.type.catacombs.events;

import dev.daweapon.type.catacombs.engine.DungeonEngine;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.dungeons.breaker.Dungeonbreaker;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.player.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Dungeonbreaker in a run. Holding one lifts the run's no-mining rule so the client breaks blocks at once, like
 * Hypixel's hidden Efficiency X and 1024 mining speed; the server then decides whether the block may go.
 */
public class ActionCatacombsDungeonbreaker implements HypixelEventClass {
    private static final double INSTANT = 1_000_000D;
    private static final double NO_MINING = 0D;
    private static final int CHECK_TICKS = 4;
    private static final int REFRESH_TICKS = 20;
    private static final long WARNING_MILLIS = 3_000;
    private static final String NEEDS_FLOOR = "<c>You must complete The Catacombs Floor III to use the Dungeonbreaker!";
    private final Map<UUID, Long> warned = new ConcurrentHashMap<>();

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onBreak(PlayerBlockBreakEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player) || player.isBypassBuild()) return;
        DungeonEngine engine = ActionCatacombsSecrets.activeEngine(player);
        if (engine == null) return;
        event.setCancelled(true);
        if (!Dungeonbreaker.is(player.getItemInMainHand()) || !Dungeonbreaker.canUse(player)) return;
        BlockVec at = event.getBlockPosition();
        engine.dig(player, at.blockX(), at.blockY(), at.blockZ());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onDig(PlayerStartDiggingEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player) || !Dungeonbreaker.is(player.getItemInMainHand())) return;
        if (ActionCatacombsSecrets.activeEngine(player) == null || Dungeonbreaker.canUse(player)) return;
        long now = System.currentTimeMillis();
        if (now - warned.getOrDefault(player.getUuid(), 0L) < WARNING_MILLIS) return;
        warned.put(player.getUuid(), now);
        player.sendMessage(NEEDS_FLOOR);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onTick(PlayerTickEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        long ticks = player.getAliveTicks();
        if (ticks % CHECK_TICKS != 0) return;
        boolean inRun = ActionCatacombsSecrets.activeEngine(player) != null;
        long now = System.currentTimeMillis();
        if (inRun && ticks % REFRESH_TICKS == 0) Dungeonbreaker.refresh(player, now);
        boolean ready = inRun && Dungeonbreaker.is(player.getItemInMainHand()) && Dungeonbreaker.canUse(player)
                && (Dungeonbreaker.isStaff(player) || Dungeonbreaker.heldCharges(player, now) > 0);
        var speed = player.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        double wanted = ready ? INSTANT : NO_MINING;
        if (speed.getBaseValue() != wanted) speed.setBaseValue(wanted);
    }
}
