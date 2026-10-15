package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Toggles whether or not you are in build mode",
        usage = "/build",
        permission = Rank.STAFF,
    labels = "buildmode build",
        allowsConsole = false)
public class BuildCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            player.setBypassBuild(!player.isBypassBuild());

            sender.sendMessage(Text.of("<a>Build mode has been " + (player.isBypassBuild() ? "<a>ENABLED" : "<c>DISABLED") + "<a>."));
        });
    }
}
