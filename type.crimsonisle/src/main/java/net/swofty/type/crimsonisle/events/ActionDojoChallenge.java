package net.swofty.type.crimsonisle.events;

import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.crimsonisle.dojo.DojoBlockedCommands;
import net.swofty.type.crimsonisle.dojo.DojoChallenge;
import net.swofty.type.crimsonisle.dojo.DojoService;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoChallengeLock;
import net.swofty.type.skyblockgeneric.event.actions.player.gui.ActionPlayerClickItem;
import net.swofty.type.skyblockgeneric.event.custom.ArrowHitBlockEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Routes arena input to the running Dojo challenge and blocks menus while one runs.
public class ActionDojoChallenge implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onAttack(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof SkyBlockPlayer player)) return;
        DojoChallenge challenge = DojoService.challengeOf(player);
        if (challenge != null) challenge.onAttack(event.getTarget());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onArrowHit(ArrowHitBlockEvent event) {
        if (!(event.getShooter() instanceof SkyBlockPlayer player)) return;
        DojoChallenge challenge = DojoService.challengeOf(player);
        if (challenge != null) challenge.onArrowHit(event.getHitPosition());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onCommand(PlayerCommandEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!DojoChallengeLock.isLocked(player) || player.getRank().isEqualOrHigherThan(Rank.STAFF)) return;
        String message = DojoBlockedCommands.messageFor(event.getCommand());
        if (message == null) return;
        event.setCancelled(true);
        player.sendMessage(message);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onUseItem(PlayerUseItemEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!DojoChallengeLock.isLocked(player)) return;
        if (player.getHeldSlot() != ActionPlayerClickItem.MENU_SLOT) return;
        event.setCancelled(true);
        DojoChallengeLock.cancel(player);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onDisconnect(PlayerDisconnectEvent event) {
        DojoService.disconnect((SkyBlockPlayer) event.getPlayer());
    }
}
