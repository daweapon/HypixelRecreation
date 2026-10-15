package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarCategories;
import dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar.GUIBazaar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "bazzar bazaar bz",
        description = "Opens the Bazaar",
        usage = "/bazzar",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class BazaarCommand extends HypixelCommand {
    private static final int REQUIRED_SKYBLOCK_LEVEL = 7;

    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!player.isBoosterCookieActive()) {
                player.sendMessage("<c>You need an active Booster Cookie to use /bazaar!");
                return;
            }
            if (player.getSkyBlockExperience().getLevel().asInt() < REQUIRED_SKYBLOCK_LEVEL) {
                player.sendMessage("<c>You need SkyBlock Level 7 to use the Bazaar!");
                return;
            }

            new GUIBazaar(BazaarCategories.FARMING).open(player);
        });
    }
}
