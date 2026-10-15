package dev.daweapon.type.replayviewer.command;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.replayviewer.TypeReplayViewerLoader;
import dev.daweapon.type.replayviewer.playback.ReplaySession;
import dev.daweapon.type.replayviewer.util.ReplayShareUtil;

@CommandParameters(
	description = "Share your current replay position",
	usage = "/share",
	permission = Rank.DEFAULT,
	allowsConsole = false,
	labels = "share"
)
public class ShareCommand extends HypixelCommand {

	@Override
	public void registerUsage(MinestomCommand command) {
		command.setDefaultExecutor((sender, _) -> {
			HypixelPlayer player = (HypixelPlayer) sender;

			var sessionOpt = TypeReplayViewerLoader.getSession(player.getUuid());
			if (sessionOpt.isEmpty()) {
				player.sendMessage("<c>You are not watching a replay!");
				return;
			}

			ReplaySession session = sessionOpt.get();
			ReplayShareUtil.sendShareCommandMessage(player, session);
		});
	}
}

