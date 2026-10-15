package dev.daweapon.type.bedwarsgame.events.custom;

import dev.daweapon.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.game.game.Game;
import dev.daweapon.type.game.game.event.GameEvent;

// Custom game event fired when a team's bed is destroyed, identifying the team and the destroyer.
public record BedDestroyedEvent(
        Game<?> game,
    TeamKey teamKey,
    BedWarsPlayer destroyer
) implements GameEvent {
    @Override
    public Game<?> getGame() {
        return game;
    }
}
