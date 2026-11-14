package dev.daweapon.type.bedwarsgame.events;

import dev.daweapon.commons.bedwars.map.BedWarsMapsConfig;
import dev.daweapon.pvp.events.FinalDamageEvent;
import dev.daweapon.type.bedwarsgame.death.BedWarsCombatTracker;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.game.game.GameState;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;

// Records player-vs-player attacks (excluding teammates) into BedWarsCombatTracker for assist credit.
public class ActionGameCombatTrack implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false)
    public void run(FinalDamageEvent event) {
        if (!(event.getEntity() instanceof BedWarsPlayer victim)) {
            return;
        }

        BedWarsGame game = victim.getGame();
        if (game == null || game.getState() != GameState.IN_PROGRESS) {
            return;
        }

        if (event.getDamage().getAttacker() instanceof BedWarsPlayer attacker) {
            if (!victim.equals(attacker) && !isSameTeam(victim, attacker)) {
                BedWarsCombatTracker.recordAttack(victim, attacker);
            }
        }
    }

    private boolean isSameTeam(BedWarsPlayer first, BedWarsPlayer second) {
        BedWarsMapsConfig.TeamKey team1 = first.getTeamKey();
        BedWarsMapsConfig.TeamKey team2 = second.getTeamKey();
        if (team1 == null || team2 == null) {
            return false;
        }
        return team1.equals(team2);
    }
}

