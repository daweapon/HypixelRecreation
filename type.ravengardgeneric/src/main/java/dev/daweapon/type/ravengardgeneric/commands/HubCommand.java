package dev.daweapon.type.ravengardgeneric.commands;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.ravengardgeneric.user.RavengardPlayer;

@CommandParameters(labels = "h hub", description = "Sends the player to the Ravengard hub",
        usage = "/hub", permission = Rank.DEFAULT, allowsConsole = false)
// Command that sends the player to the Ravengard lobby.
public class HubCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            ((RavengardPlayer) sender).sendTo(ServerType.RAVENGARD_LOBBY);
        });
    }
}
