package dev.daweapon.type.generic.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;

public final class LegacyCommand extends Command {
    public LegacyCommand(String label, String forwardedPrefix, Command target) {
        super(label);
        setCondition(target.getCondition());
        setDefaultExecutor((sender, _) -> MinecraftServer.getCommandManager().execute(sender, forwardedPrefix));
        var arguments = ArgumentType.StringArray("arguments");
        addSyntax((sender, context) -> MinecraftServer.getCommandManager().execute(sender,
                forwardedPrefix + " " + String.join(" ", context.get(arguments))), arguments);
    }
}
