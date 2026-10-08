package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.jerrysworkshop.chickenrace.ChickenRace;
import net.swofty.type.skyblockgeneric.chickenrace.ChickenRaceProgress;
import net.swofty.type.skyblockgeneric.chickenrace.Cluckpoint;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.jerry.MissionTalkToGulliver;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

// Gulliver runs the Chicken Race and only speaks chicken. Each line names the next cluckpoint to
// reach, as in the wiki's dialogue table.
public class NPCGulliver extends HypixelNPC {
	private static final Map<UUID, Integer> HEARD = new ConcurrentHashMap<>();

	public NPCGulliver() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"Gulliver",
						"<e><l>CLICK"
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "TUUGLCH9HGzfw3oC4LGEYYhCoPNa5YWAiW8XUjy5wIch9MVcMdwHYxnnphazo0OOnFutHjucI2/sv6Fq8w3VCPDYqo9yIw7RNizJK8Q9dG6H1NUOu87G9YiWygF3EutSrARXjPBuFieENVxPbkpcPvzfPGrCJUkUWSGyJVpO3pK/zzA2zy9QUg7l03lm1o0pd3baVhHZSS3OOrN9i6MfO0b6SwhsI/s4gCFoDddGYR7lMYfxUNiSI5NbhvLk3lQYYdv6YMolZx4XonJ+quB8Oq0SOcZv3Wt3dBpfMBXyDWqUjSME8KZ5D9H4u0Y1dRo9bHYjqAvLD7M+BljUqzlR9xlD8Q6TpaelqXIAUsG9CQk1Y+/sjv4SXetvWlJKL1Gqd/VL6EKHV5YUL5XZ8WeUVuY10+bPjXuWH81DvhZEGMe7ADntgbktG8CLwp1kDmXO0kyy6TgCbzkvROvhDFqRP0etYegNE/IdBDeph/2dZqkyL6dvQucoTEd+qXq5wFy/kvDC01oCB08/HIkqsm+xjFaImFvEW711s/6JD/qiBO1VECQ8xQnQ8UoH120bibH0XPS0AXqVamr/dwcUR0BdQKP1Txaq7rKtJIqPXkjRHJCv5s1QvPxGuM44629FpT6xCCoiqPwbMDZXQI7OWuq+RpZi56zxAvWKuPW//4D/dcE=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYwMjcxNTY0MTU4MywKICAicHJvZmlsZUlkIiA6ICJhYmQyYTNjNDMwYmM0NWZhODY3NmViNzYyZTMwOTI0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJYSVRzYWx2YSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84Nzc5MWFjZDQ4NjhiNWYyYjJjMTc1NmFmOTNiNTNkZTEyMGJkZjczMmIwNTZiYWQ2MDI3MjY1ZGJiMTEzNWY1IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(68.500, 105.000, 33.500, 91, 6);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player) || ChickenRace.isRacing(player)) return;
		MissionData missions = player.getMissionData();
		if (!missions.hasCompleted(MissionTalkToGulliver.class)) {
			if (!missions.isCurrentlyActive(MissionTalkToGulliver.class)) missions.startMission(MissionTalkToGulliver.class);
			missions.endMission(MissionTalkToGulliver.class);
		}
		int claimed = ChickenRaceProgress.claimed(player);
		if (claimed >= Cluckpoint.values().length) {
			sendNPCMessage(player, ThreadLocalRandom.current().nextBoolean() ? "Bak bawk bakok!" : "Coot Coot Cluck, Puk Puk Pukaaak!");
			return;
		}
		if (HEARD.getOrDefault(player.getUuid(), -1) == claimed) {
			ChickenRace.start(player);
			return;
		}
		setDialogue(player, "stage_" + claimed).thenRun(() -> {
			HEARD.put(player.getUuid(), claimed);
			ChickenRace.start(player);
		});
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		DialogueSet[] sets = new DialogueSet[Cluckpoint.values().length];
		for (int claimed = 0; claimed < sets.length; claimed++) {
			List<String> lines = new ArrayList<>();
			lines.add("Bukawk. Coot Coot Cluck, Puk Puk Pukaaak.");
			if (claimed > 0) lines.add("Cluck cluck! Bacawk! " + Cluckpoint.of(claimed).getRewardMarkup() + "<f> bakok.");
			Cluckpoint next = Cluckpoint.next(claimed);
			lines.add("Buk bawk bakok <e>Cluckpoint " + next.number() + "<f> puk pock <e>\\< " + next.getSecondsToBeat() + "s<f>?");
			lines.add("Puk Puk Pukaaak.");
			sets[claimed] = DialogueSet.builder().key("stage_" + claimed).lines(lines.toArray(String[]::new)).build();
		}
		return sets;
	}
}
