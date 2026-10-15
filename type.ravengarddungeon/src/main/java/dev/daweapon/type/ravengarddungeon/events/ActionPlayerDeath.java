package dev.daweapon.type.ravengarddungeon.events;

import net.minestom.server.event.player.PlayerDeathEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.ravengarddungeon.TypeRavengardDungeonLoader;
import dev.daweapon.type.ravengarddungeon.user.RavengardDungeonPlayer;

// Suppresses the default death chat message and eliminates the player from the dungeon game on death.
public class ActionPlayerDeath implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerDeathEvent event) {
        event.setChatMessage(null);
        RavengardDungeonPlayer player = (RavengardDungeonPlayer) event.getPlayer();
        String killer = killerName(player.getLastDamageSource());
        ScheduleUtility.nextTick(() -> TypeRavengardDungeonLoader.getGame().eliminate(player, killer));
    }

    private static String killerName(net.minestom.server.entity.damage.Damage damage) {
        if (damage == null) {
            return null;
        }
        net.minestom.server.entity.Entity attacker = damage.getAttacker() != null
                ? damage.getAttacker() : damage.getSource();
        if (attacker instanceof dev.daweapon.type.ravengardgeneric.entity.mob.RavengardMob mob) {
            return mob.displayName();
        }
        if (attacker instanceof net.minestom.server.entity.Player other) {
            return other.getUsername();
        }
        return null;
    }
}
