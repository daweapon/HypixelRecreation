package dev.daweapon.type.generic.command.commands;

import net.minestom.server.MinecraftServer;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "pl",
        description = "Lists your party members",
        usage = "/pl",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class PartyListCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> MinecraftServer.getCommandManager().execute(sender, "party list"));
    }
}
