package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.trade.TradeManager;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "trade",
        description = "Sends or accepts a trade request with a nearby player",
        usage = "/trade <player>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class TradeCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord targetArg = ArgumentType.Word("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String targetName = context.get(targetArg);

            SkyBlockPlayer target = SkyBlockGenericLoader.getLoadedPlayers().stream()
                    .filter(candidate -> candidate.getUsername().equalsIgnoreCase(targetName))
                    .findFirst().orElse(null);
            if (target == null || target.getSkyblockDataHandler() == null) {
                player.sendMessage("<c>You can only trade with players on your server!");
                return;
            }
            TradeManager.request(player, target);
        }, targetArg);
    }
}
