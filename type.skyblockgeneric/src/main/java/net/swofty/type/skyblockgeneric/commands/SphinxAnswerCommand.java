package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.mythological.ability.SphinxAbility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

@CommandParameters(labels = "sphinxanswer",
		description = "Answers a Sphinx's riddle",
		usage = "/sphinxanswer",
		permission = Rank.DEFAULT,
		allowsConsole = false)
public class SphinxAnswerCommand extends HypixelCommand {
	@Override
	public void registerUsage(HypixelCommand.MinestomCommand command) {
		ArgumentString sphinxArg = new ArgumentString("sphinx");
		ArgumentInteger choiceArg = new ArgumentInteger("choice");

		command.addSyntax((sender, context) -> {
			if (!permissionCheck(sender)) return;
			if (!(sender instanceof SkyBlockPlayer player)) return;
			UUID sphinx;
			try {
				sphinx = UUID.fromString(context.get(sphinxArg));
			} catch (IllegalArgumentException invalid) {
				return;
			}
			SphinxAbility.answer(player, sphinx, context.get(choiceArg));
		}, sphinxArg, choiceArg);
	}
}
