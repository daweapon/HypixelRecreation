package dev.daweapon.type.bedwarsgame.game.v2.listener;

import net.kyori.adventure.text.format.TextColor;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsTeam;
import dev.daweapon.type.bedwarsgame.replay.BedWarsReplayManager;
import dev.daweapon.type.bedwarsgame.stats.BedWarsStatsRecorder;
import dev.daweapon.type.game.game.event.TeamEliminatedEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;

import java.util.Optional;

// Handles a team's elimination: records bed-loss/loss stats, broadcasts it, and logs it to replay.
public class TeamEliminationListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void run(TeamEliminatedEvent<BedWarsTeam> event) {
        BedWarsTeam team = event.team();
        TextColor teamColor = team.getColor();
        String teamName = team.getName();

        BedWarsGame game = (BedWarsGame) event.game();
        team.getPlayerIds().stream()
            .map(game::getPlayer)
            .flatMap(Optional::stream)
            .forEach(player -> {
                BedWarsStatsRecorder.recordBedLost(player, game.getGameType());
                BedWarsStatsRecorder.recordLoss(player, game.getGameType());
            });

        game.broadcastMessage(Text.empty());
        game.broadcastMessage(Text.of("<f><l>TEAM ELIMINATED > </l>{} <c>has been eliminated!",
            Text.of("<color:{}>{}", teamColor, teamName)));
        game.broadcastMessage(Text.empty());

        BedWarsReplayManager replayManager = game.getReplayManager();

        // Record to replay
        if (replayManager.isRecording()) {
            replayManager.recordTeamElimination(team.getTeamKey());
        }
    }
}
