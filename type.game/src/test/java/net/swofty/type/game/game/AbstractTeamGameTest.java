package net.swofty.type.game.game;

import net.minestom.server.entity.Player;
import net.swofty.type.game.game.event.GameStateChangeEvent;
import net.swofty.type.game.game.event.GameTeamWinConditionEvent;
import net.swofty.type.game.game.team.SimpleGameTeam;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AbstractTeamGameTest {
    static class FakePlayer implements GameParticipant {
        private final UUID uuid = UUID.randomUUID();
        private String gameId;

        public UUID getUuid() {
            return uuid;
        }

        public boolean isOnline() {
            return true;
        }

        public String getGameId() {
            return gameId;
        }

        public void setGameId(String gameId) {
            this.gameId = gameId;
        }

        public Player getServerPlayer() {
            return null;
        }
    }

    static class TestGame extends AbstractTeamGame<FakePlayer, SimpleGameTeam> {
        final int maxPlayers;
        final int teamSize;

        TestGame(int maxPlayers, int teamSize, List<Object> events) {
            super(null, events::add);
            this.maxPlayers = maxPlayers;
            this.teamSize = teamSize;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public int getMinPlayers() {
            return 1000;
        }

        protected boolean isTeamViable(SimpleGameTeam team) {
            return team.hasPlayers();
        }

        protected int getTeamSize() {
            return teamSize;
        }

        void addTeam(String id) {
            registerTeam(new SimpleGameTeam(id, id, NamedTextColor.WHITE));
        }
    }

    private List<Object> events;

    @BeforeEach
    void setUp() {
        events = new ArrayList<>();
    }

    @Test
    void joinRegistersPlayerAndTagsGameId() {
        TestGame game = new TestGame(2, 1, events);
        FakePlayer player = new FakePlayer();
        assertInstanceOf(Game.JoinResult.Success.class, game.join(player));
        assertEquals(game.getGameId(), player.getGameId());
        assertEquals(1, game.getPlayers().size());
        assertEquals(1, game.getAvailableSlots());
    }

    @Test
    void joinRejectsDuplicatesAndFullGames() {
        TestGame game = new TestGame(1, 1, events);
        FakePlayer first = new FakePlayer();
        game.join(first);
        assertInstanceOf(Game.JoinResult.Denied.class, game.join(first));
        assertInstanceOf(Game.JoinResult.Denied.class, game.join(new FakePlayer()));
        assertFalse(game.canAcceptPlayers());
    }

    @Test
    void joinRejectedOnceGameStarted() {
        TestGame game = new TestGame(4, 1, events);
        game.start();
        assertEquals(GameState.IN_PROGRESS, game.getState());
        assertInstanceOf(Game.JoinResult.Denied.class, game.join(new FakePlayer()));
        assertTrue(events.stream().anyMatch(e -> e instanceof GameStateChangeEvent change && change.isGameStart()));
    }

    @Test
    void autoAssignBalancesPlayersAcrossTeams() {
        TestGame game = new TestGame(4, 2, events);
        game.addTeam("red");
        game.addTeam("blue");
        for (int i = 0; i < 4; i++) game.join(new FakePlayer());
        game.autoAssignTeams();
        assertEquals(2, game.getTeam("red").orElseThrow().getPlayerCount());
        assertEquals(2, game.getTeam("blue").orElseThrow().getPlayerCount());
        assertEquals(2, game.getActiveTeamCount());
    }

    @Test
    void autoAssignNeverExceedsTeamSize() {
        TestGame game = new TestGame(6, 1, events);
        game.addTeam("red");
        game.addTeam("blue");
        for (int i = 0; i < 4; i++) game.join(new FakePlayer());
        game.autoAssignTeams();
        long assigned = game.getTeams().stream().mapToInt(SimpleGameTeam::getPlayerCount).sum();
        assertEquals(2, assigned);
    }

    @Test
    void sameTeamRequiresBothAssigned() {
        TestGame game = new TestGame(2, 2, events);
        game.addTeam("red");
        FakePlayer a = new FakePlayer();
        FakePlayer b = new FakePlayer();
        game.join(a);
        game.join(b);
        game.autoAssignTeams();
        assertTrue(game.isSameTeam(a.getUuid(), b.getUuid()));
        assertFalse(game.isSameTeam(a.getUuid(), UUID.randomUUID()));
    }

    @Test
    void leavingRemovesPlayerFromTeamAndClearsGameId() {
        TestGame game = new TestGame(2, 2, events);
        game.addTeam("red");
        FakePlayer a = new FakePlayer();
        game.join(a);
        game.autoAssignTeams();
        game.leave(a);
        assertNull(a.getGameId());
        assertEquals(Optional.empty(), game.getPlayerTeam(a.getUuid()));
        assertEquals(0, game.getTeam("red").orElseThrow().getPlayerCount());
    }

    @Test
    void lastViableTeamIsAnnouncedAsWinnerWhenPlayerLeavesMidGame() {
        TestGame game = new TestGame(2, 1, events);
        game.addTeam("red");
        game.addTeam("blue");
        FakePlayer a = new FakePlayer();
        FakePlayer b = new FakePlayer();
        game.join(a);
        game.join(b);
        game.autoAssignTeams();
        game.start();
        events.clear();

        game.leave(a);

        GameTeamWinConditionEvent<?> win = events.stream()
                .filter(e -> e instanceof GameTeamWinConditionEvent<?>)
                .map(e -> (GameTeamWinConditionEvent<?>) e)
                .findFirst().orElseThrow();
        assertEquals(game.getPlayerTeam(b.getUuid()), win.team());
    }

    @Test
    void disconnectBeforeStartSimplyLeaves() {
        TestGame game = new TestGame(2, 1, events);
        FakePlayer a = new FakePlayer();
        game.join(a);
        game.handleDisconnect(a);
        assertFalse(game.hasDisconnectedPlayer(a.getUuid()));
        assertTrue(game.getPlayers().isEmpty());
        assertNull(a.getGameId());
    }
}
