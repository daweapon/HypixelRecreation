package dev.daweapon.type.ravengarddungeon.events;

import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.game.game.Game;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.ravengarddungeon.TypeRavengardDungeonLoader;
import dev.daweapon.type.ravengarddungeon.user.RavengardDungeonPlayer;

// On player connect, joins them into the dungeon game shortly after configuration, sending them back to the lobby if denied.
public class ActionPlayerJoin implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, phase = EventPhase.GAMEPLAY)
    public void run(AsyncPlayerConfigurationEvent event) {
        RavengardDungeonPlayer player = (RavengardDungeonPlayer) event.getPlayer();
        ScheduleUtility.delay(() -> {
            if (!player.isOnline()) return;
            for (var instance : dev.daweapon.type.ravengarddungeon.game.DungeonInstanceRegistry.all()) {
                if (instance.getPlayers().contains(player.getUuid())) {
                    return;
                }
            }
            Game.JoinResult result = TypeRavengardDungeonLoader.getGame().join(player);
            if (result instanceof Game.JoinResult.Denied(String reason)) {
                player.sendMessage("<c>Could not enter the dungeon: {}", reason);
                player.sendTo(ServerType.RAVENGARD_LOBBY);
            }
        }, 20);
    }
}
