package net.swofty.type.skyblockgeneric.event.actions.achievement;

import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumSet;
import java.util.Set;

public class ActionRegionAchievements implements HypixelEventClass {
    private static final Set<RegionType> HUB_AREAS = EnumSet.of(
            RegionType.AUCTION_HOUSE, RegionType.BANK, RegionType.BAZAAR_ALLEY, RegionType.CANVAS_ROOM,
            RegionType.COAL_MINE, RegionType.COMBAT_SETTLEMENT, RegionType.COMMUNITY_CENTER, RegionType.CRYPTS,
            RegionType.ELECTION_ROOM, RegionType.FARM, RegionType.FISHING_OUTPOST, RegionType.FORAGING_CAMP,
            RegionType.FOREST, RegionType.GRAVEYARD, RegionType.LIBRARY, RegionType.MINING_DISTRICT,
            RegionType.MOUNTAIN, RegionType.RUINS, RegionType.SEWER, RegionType.SHENS_AUCTION,
            RegionType.THAUMATURGIST, RegionType.TRADE_CENTER, RegionType.UNINCORPORATED, RegionType.VILLAGE,
            RegionType.WILDERNESS);

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerRegionChangeEvent event) {
        RegionType to = event.getTo();
        if (to == null) return;

        SkyBlockPlayer player = event.getPlayer();
        PlayerAchievementHandler achievements = player.getAchievementHandler();

        switch (to) {
            case DRAGONS_NEST -> achievements.completeAchievement("skyblock.heart_of_the_end");
            case OBSIDIAN_SANCTUARY -> achievements.completeAchievement("skyblock.into_the_deep");
            default -> {
            }
        }

        if (HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_HUB) {
            achievements.completeAchievement("skyblock.your_adventure_begins");
            checkWanderer(player, to);
        }
    }

    private static void checkWanderer(SkyBlockPlayer player, RegionType entered) {
        Set<String> visited = Set.copyOf(player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.VISITED_REGIONS, DatapointStringList.class).getValue());
        for (RegionType area : HUB_AREAS) {
            if (area != entered && !visited.contains(area.getName())) return;
        }
        player.getAchievementHandler().completeAchievement("skyblock.wanderer");
    }
}
