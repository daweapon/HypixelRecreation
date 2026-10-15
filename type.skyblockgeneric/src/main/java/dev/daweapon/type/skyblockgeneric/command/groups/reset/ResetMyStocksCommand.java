package dev.daweapon.type.skyblockgeneric.command.groups.reset;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

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
