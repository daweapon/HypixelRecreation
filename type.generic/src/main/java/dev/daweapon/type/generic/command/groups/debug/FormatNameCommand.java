package dev.daweapon.type.generic.command.groups.debug;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

import java.util.UUID;

@CommandParameters(labels = "formatname",
        description = "Gets the format of a players name from their username",
        usage = "/debug formatname <name>",
        permission = Rank.STAFF,
        allowsConsole = false)
// Staff command to look up a player's formatted/raw display name from their username.
public class FormatNameCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString playerArgument = ArgumentType.String("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String playerName = context.get(playerArgument);
            HypixelPlayer player = (HypixelPlayer) sender;
            UUID uuid = HypixelDataHandler.getPotentialUUIDFromName(playerName);

            if (uuid == null) {
                player.sendMessage("Nope doesn't exist");
                return;
            }

            player.sendMessage(HypixelPlayer.getDisplayName(uuid));
            player.sendMessage(HypixelPlayer.getRawName(uuid));
        }, playerArgument);
    }
}
