package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.TeamColorUtil;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Builds and assigns the scoreboard team that renders a player's SkyBlock level in their nametag
public class ActionAddSkyBlockXPToNametag implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerSpawnEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!event.isFirstSpawn()) return;

        ScheduleUtility.delay(() -> {
            updatePlayerNametag(player);
        }, 15);
    }

    public static void updatePlayerNametag(SkyBlockPlayer player) {
        if (!player.isOnline()) return;

        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        Rank rank = player.getRank();

        String teamName = StringUtility.limitStringLength(rank.getPriorityCharacter() + player.getUsername(), 15);
        Text levelPrefix = Text.of("<8>[")
                .append(Text.parse(experience.getLevel().getColor() + experience.getLevel()))
                .append("<8>] ");
        Team team = new TeamBuilder("Z" + teamName, MinecraftServer.getTeamManager())
            .prefix(levelPrefix.asComponent().append(player.getRankPrefix()))
                .teamColor(TeamColorUtil.fromNamedColor(rank.getTextColor()))
                .build();
        player.setTeam(team);
        player.getTeam().sendUpdatePacket();
    }
}
