package dev.daweapon.type.bedwarsgame.events;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.event.player.PlayerChatEvent;
import dev.daweapon.commons.bedwars.BedWarsGameType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.chat.ChatChannelRouter;
import dev.daweapon.type.generic.collectibles.bedwars.prestige.BedWarsPrestigeRenderer;
import dev.daweapon.type.generic.data.handlers.BedWarsDataHandler;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.categories.Rank;

import java.util.ArrayList;
import java.util.List;

// Handles in-game Bedwars chat: routes messages by chat type (all/team/staff) and sanitizes input.
public class ActionPlayerChat implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
	public void run(PlayerChatEvent event) {
		if (event.isCancelled()) return;
		final BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
		event.setCancelled(true);

		BedWarsGame game = player.getGame();
		if (game == null) return;

		BedWarsDataHandler bedWarsDataHandler = BedWarsDataHandler.getUser(player);
		if (bedWarsDataHandler == null) {
			player.sendMessage("<c>An error occurred while processing your chat message. Please try again later.");
			return;
		}

		Rank rank = player.getRank();
		String finalMessage = ChatChannelRouter.route(player, event.getRawMessage()).orElse(null);
		if (finalMessage == null) return;

		if (game.getState().isWaiting()) {
			TextColor messageColor = rank.equals(Rank.DEFAULT) ? NamedTextColor.GRAY : NamedTextColor.WHITE;

			game.getPlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(
					"{}<color:{}>: {}",
					player.getFullDisplayName(), messageColor, finalMessage));
			return;
		}

		if (game.getReplayManager().isRecording()) {
			game.getReplayManager().recordPlayerChat(player, finalMessage, false);
		}

		List<BedWarsPlayer> receivers;
		if (game.getGameType() == BedWarsGameType.ONE_EIGHT || game.getGameType() == BedWarsGameType.ONE_BLOCK) {
			receivers = new ArrayList<>(game.getPlayers());
		} else {
			receivers = game.getPlayersOnTeam(player.getTeamKey());
		}

		Text levelPrefix = BedWarsPrestigeRenderer.renderBrackets(player).append(" ");
		TextColor messageColor = rank.equals(Rank.DEFAULT) ? NamedTextColor.GRAY : NamedTextColor.WHITE;

		receivers.forEach(onlinePlayer -> onlinePlayer.sendMessage(
				"{}{}<color:{}>: {}",
				levelPrefix, player.getFullDisplayName(), messageColor, finalMessage));
	}
}
