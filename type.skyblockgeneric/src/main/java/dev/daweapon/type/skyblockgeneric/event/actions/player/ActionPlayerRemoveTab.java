package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import dev.daweapon.commons.TeamColorUtil;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Collections;

// Sends custom scoreboard teams between players so tab-list name prefixes stay accurate on spawn
public class ActionPlayerRemoveTab implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerSpawnEvent event) {
        Player player = event.getPlayer();

        SkyBlockGenericLoader.forEachLoadedPlayer(player2 -> {
            Rank player2Rank = player2.getRank();
            Rank playerRank = ((SkyBlockPlayer) player).getRank();

            if (player2.getUuid().equals(player.getUuid())) return;

            Component playerPrefix = playerRank.prefixComponent();
            player2.sendPacket(new TeamsPacket("ZZZZZ" + player.getUsername(), new TeamsPacket.CreateTeamAction(
                    new TeamsPacket.Settings(
                            playerPrefix,
                            playerPrefix,
                            Component.empty(),
                            TeamsPacket.NameTagVisibility.ALWAYS,
                            TeamsPacket.CollisionRule.ALWAYS,
                            TeamColorUtil.fromNamedColor(playerRank.getTextColor()),
                            (byte) 0x01
                    ),
                    new ArrayList<>(Collections.singletonList(player.getUsername()))
            )));
            Component player2Prefix = player2Rank.prefixComponent();
            player.sendPacket(new TeamsPacket("ZZZZZ" + player2.getUsername(), new TeamsPacket.CreateTeamAction(
                    new TeamsPacket.Settings(
                            player2Prefix,
                            player2Prefix,
                            Component.empty(),
                            TeamsPacket.NameTagVisibility.ALWAYS,
                            TeamsPacket.CollisionRule.ALWAYS,
                            TeamColorUtil.fromNamedColor(player2Rank.getTextColor()),
                            (byte) 0x01
                    ),
                    new ArrayList<>(Collections.singletonList(player2.getUsername()))
            )));
        });
    }
}
