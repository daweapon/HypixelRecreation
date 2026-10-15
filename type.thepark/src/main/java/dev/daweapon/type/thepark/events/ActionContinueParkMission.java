package dev.daweapon.type.thepark.events;

import net.minestom.server.entity.Entity;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.birchpark.MissionTravelToThePark;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.darkthicket.MissionFindTheCampfire;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.darkthicket.MissionTravelToTheDarkThicket;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.jungle.MissionTalkToMolbertAgainAgainAgain;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.savanna.MissionTravelToTheSavannaWoodland;
import dev.daweapon.type.skyblockgeneric.mission.missions.thepark.spruce.MissionTravelToTheSpruceWoods;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.thepark.TypeTheParkLoader;
import dev.daweapon.type.thepark.npcs.NPCWorkerXavier;

// Advances The Park's region-travel missions as a player moves between its sub-areas.
public class ActionContinueParkMission implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerRegionChangeEvent event) {
        if (event.getTo() == null) return;

		SkyBlockPlayer player = event.getPlayer();
        MissionData data = event.getPlayer().getMissionData();
		switch (event.getTo()) {
			case BIRCH_PARK -> {
				if (data.isCurrentlyActive(MissionTravelToThePark.class)) {
					data.endMission(MissionTravelToThePark.class);
				}
			}
			case SPRUCE_WOODS -> {
				if (data.isCurrentlyActive(MissionTravelToTheSpruceWoods.class)) {
					data.endMission(MissionTravelToTheSpruceWoods.class);
				}
			}
			case DARK_THICKET -> {
				if (data.isCurrentlyActive(MissionTravelToTheDarkThicket.class)) {
					data.endMission(MissionTravelToTheDarkThicket.class);
				}
			}
			case TRIALS_OF_FIRE -> {
				if (data.isCurrentlyActive(MissionFindTheCampfire.class)) {
					data.endMission(MissionFindTheCampfire.class);
				}
			}
			case SAVANNA_WOODLAND -> {
				if (data.isCurrentlyActive(MissionTravelToTheSavannaWoodland.class)) {
					NPCWorkerXavier xavier = HypixelNPC.getRegisteredNPCs().stream()
							.filter(npc -> npc instanceof NPCWorkerXavier)
							.map(npc -> (NPCWorkerXavier) npc)
							.findFirst()
							.orElse(null);
					if (xavier == null) {
						return;
					}
					xavier.setDialogue(player, "intro").thenRun(() -> {
						data.endMission(MissionTravelToTheSavannaWoodland.class);
					});
				}
			}
			case JUNGLE_ISLAND -> {
				if (data.isCurrentlyActive(MissionTalkToMolbertAgainAgainAgain.class)) {
					TypeTheParkLoader.entities.forEach(Entity::updateViewableRule);
				}
			}
			case null, default -> {
			}
		}

    }
}
