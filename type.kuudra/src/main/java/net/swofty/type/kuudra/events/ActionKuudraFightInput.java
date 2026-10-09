package net.swofty.type.kuudra.events;

import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerInputEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.kuudra.fight.KuudraFight;
import net.swofty.type.kuudra.fight.world.KuudraItems;
import net.swofty.type.kuudra.run.KuudraRun;
import net.swofty.type.kuudra.run.KuudraRuns;
import net.swofty.type.skyblockgeneric.entity.ArrowEntityImpl;
import net.swofty.type.skyblockgeneric.event.custom.SalvationBeamEvent;
import net.swofty.type.skyblockgeneric.item.handlers.bow.ArrowDamage;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Routes player input inside a Kuudra run to its fight. The handlers run before SkyBlock's own gameplay
 * listeners, so cancelling here keeps abilities from firing while a player carries supplies or sits in a seat.
 */
public class ActionKuudraFightInput implements HypixelEventClass {

    private static @Nullable KuudraFight fightOf(Player player) {
        KuudraRun run = KuudraRuns.of(player.getUuid());
        return run == null ? null : run.fight();
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onUse(PlayerUseItemEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        KuudraFight fight = fightOf(player);
        if (fight == null) {
            if (KuudraItems.kindOf(event.getItemStack()) != null) event.setCancelled(true);
            return;
        }
        if (fight.useItem(player, event.getItemStack()) || fight.locksHands(player)) event.setCancelled(true);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onBeginUse(PlayerBeginItemUseEvent event) {
        if (event.getPlayer() instanceof SkyBlockPlayer player && locked(player)) event.setCancelled(true);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onBlockUse(PlayerBlockInteractEvent event) {
        if (event.getPlayer() instanceof SkyBlockPlayer player && locked(player)) {
            event.setCancelled(true);
            event.setBlockingItemUse(true);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onPlace(PlayerBlockPlaceEvent event) {
        ItemStack held = event.getPlayer().getItemInHand(event.getHand());
        if (KuudraItems.kindOf(held) == null) return;
        event.setCancelled(true);
        if (event.getPlayer() instanceof SkyBlockPlayer player) {
            KuudraFight fight = fightOf(player);
            if (fight != null) fight.useItem(player, held);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onSwing(PlayerHandAnimationEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        KuudraFight fight = fightOf(player);
        if (fight != null && fight.swing(player)) event.setCancelled(true);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onSlot(PlayerChangeHeldSlotEvent event) {
        if (event.getPlayer() instanceof SkyBlockPlayer player && locked(player)) event.setCancelled(true);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onInput(PlayerInputEvent event) {
        if (!event.hasPressedShiftKey() || !(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        KuudraFight fight = fightOf(player);
        if (fight != null) fight.sneak(player);
    }

    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onAttack(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof SkyBlockPlayer player)) return;
        KuudraFight fight = fightOf(player);
        if (fight != null) fight.attack(player, event.getTarget());
    }

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onArrow(ProjectileCollideWithEntityEvent event) {
        if (!(event.getEntity() instanceof ArrowEntityImpl arrow) || !(arrow.getShooter() instanceof SkyBlockPlayer player)) return;
        KuudraFight fight = fightOf(player);
        if (fight == null || !fight.isKuudra(event.getTarget())) return;
        fight.arrowHitKuudra(player, ArrowDamage.roll(player, arrow, ItemStatistics.empty()).getKey());
        ArrowDamage.onLanded(player, arrow);
        arrow.remove();
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onBeam(SalvationBeamEvent event) {
        KuudraFight fight = fightOf(event.shooter());
        if (fight != null && event.targets().stream().anyMatch(fight::isKuudra)) fight.arrowHitKuudra(event.shooter(), event.damage());
    }

    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false, phase = EventPhase.POST_SPAWN)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof SkyBlockPlayer player)) return;
        KuudraRun run = KuudraRuns.of(player.getUuid());
        if (run == null) return;
        KuudraFight fight = run.fight();
        if (fight == null) {
            event.setCancelled(true);
            return;
        }
        fight.damaged(player, event);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onLeave(PlayerDisconnectEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        for (KuudraRun run : KuudraRuns.all()) {
            KuudraFight fight = run.fight();
            if (fight != null && run.isMember(player.getUuid())) fight.leave(player);
        }
        KuudraItems.strip(player);
    }

    private static boolean locked(SkyBlockPlayer player) {
        KuudraFight fight = fightOf(player);
        return fight != null && fight.locksHands(player);
    }
}
