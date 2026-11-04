package dev.daweapon.type.skyblockgeneric.command.groups.force;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.commands.VisitorCommand;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.command.builder.arguments.ArgumentWord;

@CommandParameters(labels = "visitor", description = "Summons any Garden visitor to your Garden",
        usage = "/force visitor <visitor>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceVisitorCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord visitor = VisitorCommand.visitorArgument();
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            VisitorCommand.spawn((SkyBlockPlayer) sender, context.get(visitor));
        }, visitor);
    }
}
