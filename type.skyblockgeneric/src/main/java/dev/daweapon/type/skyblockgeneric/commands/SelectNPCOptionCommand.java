package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "selectnpcoption",
		description = "Selects an option from an NPC dialogue",
		usage = "/selectnpcoption",
		permission = Rank.DEFAULT,
		allowsConsole = false)
public class SelectNPCOptionCommand extends HypixelCommand {
	@Override
	public void registerUsage(HypixelCommand.MinestomCommand command) {
		ArgumentString npcArg = new ArgumentString("npc");
		ArgumentString optionArg = new ArgumentString("option");

		command.addSyntax((sender, context) -> {
			if (!permissionCheck(sender)) return;
			String npc = context.get(npcArg);
			String option = context.get(optionArg);

			if (!(sender instanceof HypixelPlayer player)) {
				return;
			}

			NPCOption.OptionData data = NPCOption.options.get(player);
			if (data == null || !data.npcId().equals(npc)) {
				return;
			}

			for (NPCOption.Option opt : data.options()) {
				if (opt.key().equalsIgnoreCase(option)) {
					opt.action().accept(player);
					NPCOption.options.remove(player);
					break;
				}
			}
		}, npcArg, optionArg);
	}
}
