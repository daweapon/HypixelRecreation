package dev.daweapon.type.bedwarsgame.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.commons.bedwars.BedwarsLevelUtil;
import dev.daweapon.type.generic.data.datapoints.DatapointLeaderboardLong;
import dev.daweapon.type.generic.data.handlers.BedWarsDataHandler;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.user.HypixelPlayer;

// Loads a player's Bedwars data and applies their level/experience bar on first spawn.
public class ActionPlayerDataSpawn implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER_DATA, requireDataLoaded = false, isAsync = true)
    public void run(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        BedWarsDataHandler handler = BedWarsDataHandler.getUser(player.getUuid());
        handler.runOnLoad(player);

        DatapointLeaderboardLong dp = handler.get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class);
        player.setLevel(BedwarsLevelUtil.calculateLevel(dp.getValue()));
        player.setExp((float) BedwarsLevelUtil.calculateExperienceSinceLastLevel(dp.getValue()) / BedwarsLevelUtil.calculateMaxExperienceFromExperience(dp.getValue()));

        // should be handled in BedWarsPlayer
        //List<HypixelPlayer> players = HypixelGenericLoader.getLoadedPlayers();
        //Collection<Player> playerCollection = players.stream().map(p -> (Player) p).toList();
        //PacketSendingUtils.sendGroupedPacket(playerCollection, new PlayerInfoRemovePacket(player.getUuid()));
    }
}