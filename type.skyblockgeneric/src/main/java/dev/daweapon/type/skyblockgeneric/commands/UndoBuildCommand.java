package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.building.BuildUndo;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

@CommandParameters(description = "Undoes your last Builder's Wand or Block Zapper use", usage = "/undobuild <wand|zap>",
        permission = Rank.DEFAULT, labels = "undobuild", allowsConsole = false)
public class UndoBuildCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord kindArg = ArgumentType.Word("kind").from("wand", "zap");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            BuildUndo.undo((SkyBlockPlayer) sender, BuildUndo.Kind.valueOf(context.get(kindArg).toUpperCase()));
        }, kindArg);
    }
}
