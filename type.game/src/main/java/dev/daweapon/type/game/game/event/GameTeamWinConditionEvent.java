package dev.daweapon.type.game.game.event;

import dev.daweapon.type.game.game.Game;
import dev.daweapon.type.game.game.team.GameTeam;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record GameTeamWinConditionEvent<T extends GameTeam>(
        @NotNull Game<?> game,
	Optional<T> team
) implements GameEvent {
	@Override
    public Game<?> getGame() {
        return game;
	}
}
