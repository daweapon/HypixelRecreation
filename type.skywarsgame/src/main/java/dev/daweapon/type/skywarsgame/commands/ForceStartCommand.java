package dev.daweapon.type.skywarsgame.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.game.SkywarsGameStatus;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;

@CommandParameters(
        description = "Force starts the skywars game",
        usage = "/forcestart [seconds]",
        permission = Rank.STAFF,
        allowsConsole = false,
        labels = "forcestart"
)
public class ForceStartCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof SkywarsPlayer player)) return;

            SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
            if (game == null) {
                player.sendMessage("<c>You are not in a game!");
                return;
            }

            if (game.getGameStatus() != SkywarsGameStatus.WAITING &&
                    game.getGameStatus() != SkywarsGameStatus.STARTING) {
                player.sendMessage("<c>The game has already started!");
                return;
            }

            game.forceStart(5);
            player.sendMessage("<a>Force starting the game in 5 seconds!");
        });

        var secondsArg = ArgumentType.Integer("seconds").min(1).max(60);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof SkywarsPlayer player)) return;

            SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
            if (game == null) {
                player.sendMessage("<c>You are not in a game!");
                return;
            }

            if (game.getGameStatus() != SkywarsGameStatus.WAITING &&
                    game.getGameStatus() != SkywarsGameStatus.STARTING) {
                player.sendMessage("<c>The game has already started!");
                return;
            }

            int seconds = context.get(secondsArg);
            game.forceStart(seconds);
            player.sendMessage("<a>Force starting the game in {} seconds!", seconds);
        }, secondsArg);
    }
}
