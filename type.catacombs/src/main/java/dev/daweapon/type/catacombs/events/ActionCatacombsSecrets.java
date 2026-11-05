package dev.daweapon.type.catacombs.events;

import dev.daweapon.type.catacombs.engine.DungeonEngine;
import dev.daweapon.type.catacombs.run.CatacombsLifecycle;
import dev.daweapon.type.catacombs.run.CatacombsRun;
import dev.daweapon.type.catacombs.run.CatacombsRuns;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;

/** Right-clicking a secret chest, a Wither Essence or a crypt tomb inside a run. */
public class ActionCatacombsSecrets implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onInteract(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN || !(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        CatacombsRun run = CatacombsRuns.of(player.getUuid());
        if (run == null || run.lifecycle().stage() != CatacombsLifecycle.Stage.ACTIVE) return;
        DungeonEngine engine = run.engine();
        if (engine == null || run.ghosts().isGhost(player.getUuid())) return;
        BlockVec at = event.getBlockPosition();
        if (!engine.interact(player, at.blockX(), at.blockY(), at.blockZ())) return;
        event.setCancelled(true);
        event.setBlockingItemUse(true);
    }
}
