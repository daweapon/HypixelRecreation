package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.romero.RomeroJulietteQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "romerorestore",
		description = "Buys back a lost Romero and Juliette quest item",
		usage = "/romerorestore",
		permission = Rank.DEFAULT,
		allowsConsole = false)
public class RomeroRestoreCommand extends HypixelCommand {
	@Override
	public void registerUsage(MinestomCommand command) {
		command.setDefaultExecutor((sender, _) -> {
			if (!permissionCheck(sender)) return;

			SkyBlockPlayer player = (SkyBlockPlayer) sender;
			if (RomeroJulietteQuest.restoreItem(player)) {
				player.sendMessage("<a>Romero handed you a new gift for Juliette!");
			}
		});
	}
}
