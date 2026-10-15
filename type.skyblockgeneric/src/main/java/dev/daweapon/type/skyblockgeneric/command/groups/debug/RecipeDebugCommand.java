package dev.daweapon.type.skyblockgeneric.command.groups.debug;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "recipes", description = "Prints recipe debug", usage = "/debug recipes", permission = Rank.STAFF, allowsConsole = false)
public class RecipeDebugCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;


        });
    }
}
