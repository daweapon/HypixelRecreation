package dev.daweapon.type.generic.command.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "gchat gc",
        description = "Sends a message to guild chat",
        usage = "/gchat <message>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class GuildChatCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        var message = ArgumentType.StringArray("message");
        command.addSyntax((sender, context) -> MinecraftServer.getCommandManager().execute(
                sender, "guild chat " + String.join(" ", context.get(message))), message);
    }
}
