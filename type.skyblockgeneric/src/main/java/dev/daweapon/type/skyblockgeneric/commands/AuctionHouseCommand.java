package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.auction.GUIAuctionHouse;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "auctionhouse ah",
        description = "Opens the Auction House",
        usage = "/auctionhouse",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class AuctionHouseCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!player.isBoosterCookieActive()) {
                player.sendMessage("<c>You need an active Booster Cookie to use /ah!");
                return;
            }

            new GUIAuctionHouse().open(player);
        });
    }
}
