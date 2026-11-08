package dev.daweapon.type.skyblockgeneric.mission.missions.blacksmith;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.mission.LocationAssociatedMission;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MissionReforgeAnItem extends SkyBlockMission implements LocationAssociatedMission {
    public static final ItemType PRICE_ITEM = ItemType.COAL;
    public static final int PRICE_AMOUNT = 10;
    private static final double MINING_XP = 150;

    public static boolean paysWithCoal(SkyBlockPlayer player) {
        return player.getMissionData().isCurrentlyActive(MissionReforgeAnItem.class);
    }

    @Override
    public String getID() {
        return "reforge_an_item";
    }

    @Override
    public String getName() {
        return "Reforge an item";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        mission.getNewObjectiveText().forEach(player::sendMessage);
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        mission.getObjectiveCompleteText(new ArrayList<>(List.of("<3>150 <7>Mining Experience"))).forEach(player::sendMessage);
        player.getSkills().increase(player, SkillCategories.MINING, MINING_XP);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.MINING_DISTRICT, RegionType.COAL_MINE);
    }

    @Override
    public Pos getLocation() {
        return new Pos(10.5, 63, -126.5);
    }
}
