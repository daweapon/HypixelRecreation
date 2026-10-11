package net.swofty.type.skyblockgeneric.command.groups.reset;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "stocks",
        description = "Resets your shop stocks back to MAXIMUM",
        usage = "/reset stocks",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ResetMyStocksCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            player.getShoppingData().resetStocks();
            player.sendMessage("<a>Whoop!");
        });
    }
}
