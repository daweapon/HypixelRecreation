package dev.daweapon.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "transfer sendto",
        description = "Transfers a player to another server",
        usage = "/sendto <server_type>",
        permission = Rank.STAFF,
        allowsConsole = false)
// Staff command that transfers the sender to a server of the given ServerType.
public class SendToCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<ServerType> serverType = new ArgumentEnum<>("server_type", ServerType.class);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            ServerType type = context.get(serverType);
            HypixelPlayer player = (HypixelPlayer) sender;

            player.sendTo(type, true);
        }, serverType);
    }
}
