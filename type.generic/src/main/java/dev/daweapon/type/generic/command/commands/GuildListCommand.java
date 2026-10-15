package dev.daweapon.type.generic.command.commands;

import net.minestom.server.MinecraftServer;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "gl",
        description = "Lists your guild members",
        usage = "/gl",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class GuildListCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> MinecraftServer.getCommandManager().execute(sender, "guild list"));
    }
}
