package net.swofty.type.generic.command.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "ochat oc",
        description = "Sends a message to guild chat",
        usage = "/ochat <message>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class OfficerChatCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        var message = ArgumentType.StringArray("message");
        command.addSyntax((sender, context) -> MinecraftServer.getCommandManager().execute(
                sender, "guild officerchat " + String.join(" ", context.get(message))), message);
    }
}
