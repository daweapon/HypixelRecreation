package dev.daweapon.type.mainlobby.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.bedwars.BedwarsLevelUtil;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.data.datapoints.DatapointLeaderboardLong;
import dev.daweapon.type.generic.data.handlers.BedWarsDataHandler;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

// Announces staff joins and loads BedWars level/experience display when spawning in the BedWars lobby.
public class ActionPlayerDataSpawn implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER_DATA, requireDataLoaded = false, isAsync = true)
    public void run(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        if (!(HypixelConst.getTypeLoader().getType() == ServerType.BEDWARS_LOBBY)) return;

        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        Rank rank = player.getRank();
        if (rank == Rank.DEFAULT) return;

        for (HypixelPlayer onlinePlayer : HypixelGenericLoader.getLoadedPlayers()) {
            onlinePlayer.sendMessage("{} <6>joined the lobby!", player.getFullDisplayName());
        }

        BedWarsDataHandler handler = BedWarsDataHandler.getUser(player.getUuid());
        handler.runOnLoad(player);

        // display the player level progression in the experience bar
        DatapointLeaderboardLong dp = handler.get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class);
        player.setLevel(BedwarsLevelUtil.calculateLevel(dp.getValue()));
        player.setExp((float) BedwarsLevelUtil.calculateExperienceSinceLastLevel(dp.getValue()) / BedwarsLevelUtil.calculateMaxExperienceFromExperience(dp.getValue()));
    }
}