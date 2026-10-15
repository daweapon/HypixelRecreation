package dev.daweapon.type.skyblockgeneric.mission.missions;


import dev.daweapon.type.generic.data.datapoints.DatapointDouble;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.*;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class MissionUseTeleporter extends SkyBlockMission {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onRegionChange(PlayerRegionChangeEvent event) {
        if (event.getTo() == null || !event.getTo().equals(RegionType.VILLAGE)) {
            return;
        }

        if (!event.getPlayer().getMissionData().isCurrentlyActive(this.getClass())) {
            return;
        }

        MissionData data = event.getPlayer().getMissionData();
        data.endMission(this.getClass());
    }

    @Override
    public String getID() {
        return "use_teleporter";
    }

    @Override
    public String getName() {
        return "Use the teleporter";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        mission.getNewObjectiveText().forEach(player::sendMessage);
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        mission.getObjectiveCompleteText(new ArrayList<>(List.of("<6>1000 <7>Coins"))).forEach(player::sendMessage);
        player.getSkyblockDataHandler().get(dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler.Data.COINS, DatapointDouble.class).setValue(
                player.getSkyblockDataHandler().get(dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler.Data.COINS, DatapointDouble.class).getValue() + 1000
        );
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Collections.singleton(RegionType.PRIVATE_ISLAND);
    }
}
