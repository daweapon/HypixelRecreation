package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.gui.inventories.bazaar.GUIBazaar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
