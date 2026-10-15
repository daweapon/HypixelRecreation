package dev.daweapon.type.bedwarslobby.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.bedwars.BedwarsLevelUtil;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.bedwarslobby.hologram.LeaderboardHologramManager;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.achievement.AchievementCategory;
import dev.daweapon.type.generic.data.datapoints.DatapointLeaderboardLong;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.data.handlers.BedWarsDataHandler;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.quest.QuestData;
import dev.daweapon.type.generic.quest.QuestDefinition;
import dev.daweapon.type.generic.quest.QuestRegistry;
import dev.daweapon.type.generic.quest.QuestType;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

// Loads a player's Bedwars data on first spawn: level bar, join announcement, holograms, and quests.
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

		// Spawn leaderboard holograms for this player
		LeaderboardHologramManager.spawnHologramsForPlayer(player);

		// Auto-accept quests if enabled and player is MVP+
		if (player.getRank().isEqualOrHigherThan(Rank.MVP_PLUS) &&
			player.getToggles().get(DatapointToggles.Toggles.ToggleType.AUTO_ACCEPT_QUESTS)) {
			autoAcceptQuests(player, AchievementCategory.BEDWARS);
		}
    }

	private void autoAcceptQuests(HypixelPlayer player, AchievementCategory category) {
		QuestData questData = player.getQuestHandler().getQuestData();
		int startedCount = 0;

		// Auto-accept daily quests
		for (QuestDefinition quest : QuestRegistry.getByCategory(category, QuestType.DAILY)) {
			if (!questData.isActive(quest.getId()) && !questData.isCompleted(quest.getId())) {
				player.getQuestHandler().startQuest(quest.getId());
				startedCount++;
			}
		}

		// Auto-accept weekly quests
		for (QuestDefinition quest : QuestRegistry.getByCategory(category, QuestType.WEEKLY)) {
			if (!questData.isActive(quest.getId()) && !questData.isCompleted(quest.getId())) {
				player.getQuestHandler().startQuest(quest.getId());
				startedCount++;
			}
		}

		if (startedCount > 0) {
			player.sendMessage("<a>You have automatically started {} {} quest{}!",
					startedCount, category.getDisplayName(), startedCount > 1 ? "s" : "");
		}
	}
}