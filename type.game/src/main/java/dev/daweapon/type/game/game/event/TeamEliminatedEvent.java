package dev.daweapon.type.game.game.event;

import dev.daweapon.type.game.game.AbstractTeamGame;
import dev.daweapon.type.game.game.Game;
import dev.daweapon.type.game.game.team.GameTeam;

public record TeamEliminatedEvent<T extends GameTeam>(
        AbstractTeamGame<?, T> game,
        T team
) implements GameEvent {
    @Override
    public Game<?> getGame() {
        return game;
    }
}
