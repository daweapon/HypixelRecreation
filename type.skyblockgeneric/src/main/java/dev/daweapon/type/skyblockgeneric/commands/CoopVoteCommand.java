package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.CoopVoteManager;

@CommandParameters(labels = "coopvote",
        description = "Votes on an active co-op add/kick vote",
        usage = "/coopvote <accept|deny>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopVoteCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord choiceArg = ArgumentType.Word("choice");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String choice = context.get(choiceArg);

            Boolean accept = switch (choice.toLowerCase()) {
                case "accept", "yes" -> true;
                case "deny", "no" -> false;
                default -> null;
            };

            if (accept == null) {
                player.sendMessage("<c>Usage: {}", "/coopvote <accept|deny>");
                return;
            }

            String error = CoopVoteManager.castVote(player, accept);
            if (error != null) {
                player.sendMessage("<b>[Co-op] <c>{}", error);
                return;
            }

            player.sendMessage("<b>[Co-op] <e>Your vote has been cast.");
        }, choiceArg);
    }
}
