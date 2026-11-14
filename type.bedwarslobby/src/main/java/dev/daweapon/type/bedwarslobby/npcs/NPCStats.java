package dev.daweapon.type.bedwarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.bedwars.BedWarsModeStats;
import dev.daweapon.commons.bedwars.BedwarsLeaderboardMode;
import dev.daweapon.commons.bedwars.BedwarsLeaderboardPeriod;
import dev.daweapon.commons.bedwars.BedwarsLevelUtil;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.bedwarslobby.gui.GUIBedWarsStatistics;
import dev.daweapon.type.generic.collectibles.bedwars.prestige.BedWarsPrestigeRenderer;
import dev.daweapon.type.generic.data.datapoints.DatapointBedWarsModeStats;
import dev.daweapon.type.generic.data.datapoints.DatapointLeaderboardLong;
import dev.daweapon.type.generic.data.handlers.BedWarsDataHandler;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;

import static dev.daweapon.commons.bedwars.BedwarsLevelUtil.suffix;

// NPC that displays a live summary of the viewing player's Bedwars stats and opens the stats GUI on click.
public class NPCStats extends HypixelNPC {

	public NPCStats() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				BedWarsDataHandler handler = BedWarsDataHandler.getUser(player);
				int level = 0;
				int progress = 0;
				int maxExperience = 0;
				long totalWins = 0;
				long winstreak = 0;
				if (handler != null) {
					long experience = handler.get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class).getValue();
					level = BedwarsLevelUtil.calculateLevel(experience);
					progress = BedwarsLevelUtil.calculateExperienceSinceLastLevel(experience);
					maxExperience = BedwarsLevelUtil.calculateMaxExperienceFromExperience(experience);

					BedWarsModeStats modeStats = handler.get(BedWarsDataHandler.Data.MODE_STATS, DatapointBedWarsModeStats.class).getValue();
					totalWins = modeStats.getWins(BedwarsLeaderboardMode.ALL, BedwarsLeaderboardPeriod.LIFETIME);
					winstreak = modeStats.getWinstreak(BedwarsLeaderboardMode.ALL);
				}
				return new String[]{
						"<6><l>Bed Wars Profile",
						Text.of("<f>Your Level: {}", BedWarsPrestigeRenderer.renderBrackets(player, level)).serialize(),
						Text.of("<f>Progress: <b>{}<7>/<a>{}", suffix(progress), suffix(maxExperience)).serialize(),
						Text.of("<f>Achievements: <e>{}<a>/{}", 0, 0).serialize(),
						Text.of("<f>Total Wins: <a>{}", suffix(totalWins)).serialize(),
						Text.of("<f>Current Winstreak: <a>{}", suffix(winstreak)).serialize(),
						"<e><l>CLICK FOR STATS",
				};
			}

			@Override
			public String texture(HypixelPlayer player) {
				return player.getPlayerSkin().textures();
			}

			@Override
			public String signature(HypixelPlayer player) {
				return player.getPlayerSkin().signature();
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(2, 67, -17, 65, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return false;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		new GUIBedWarsStatistics().open(event.player());
	}
}
