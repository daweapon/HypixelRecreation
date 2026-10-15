package dev.daweapon.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "gm gamemode",
        description = "Sets a players gamemode",
        usage = "/gamemode <gamemode>",
        permission = Rank.STAFF,
        allowsConsole = false)
// Staff command to change a player's Minecraft gamemode.
public class GamemodeCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<GameMode> gamemode = ArgumentType.Enum("gamemode", GameMode.class);
        gamemode.setFormat(ArgumentEnum.Format.LOWER_CASED);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            final GameMode gamemodeType = context.get(gamemode);

            player.setGameMode(gamemodeType);

            player.sendMessage("<a>Set your gamemode to <e>{}<a>.", gamemodeType.name());
        }, gamemode);
    }
}
