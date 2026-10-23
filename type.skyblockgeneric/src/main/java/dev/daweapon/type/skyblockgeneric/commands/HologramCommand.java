package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.furniture.HologramEditing;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;

@CommandParameters(description = "Sets the line of a Hologram you are editing", usage = "/hgl <text|clear>",
        permission = Rank.DEFAULT, labels = "hgl", allowsConsole = false)
public class HologramCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentStringArray textArg = ArgumentType.StringArray("text");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HologramEditing.apply((SkyBlockPlayer) sender, String.join(" ", context.get(textArg)));
        }, textArg);
    }
}
