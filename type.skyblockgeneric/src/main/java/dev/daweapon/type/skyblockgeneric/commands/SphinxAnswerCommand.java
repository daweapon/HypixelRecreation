package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.mythological.ability.SphinxAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
