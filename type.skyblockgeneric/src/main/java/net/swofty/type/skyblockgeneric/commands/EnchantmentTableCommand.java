package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.gui.inventories.GUIEnchantmentTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "etable et enchantingtable enchantmenttable enchantingtablemenu enchantmenttablemenu",
        description = "Opens an Enchantment Table remotely",
        usage = "/etable",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class EnchantmentTableCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!player.isBoosterCookieActive()) {
                player.sendMessage("<c>You need an active Booster Cookie to use /etable!");
                return;
            }
            new GUIEnchantmentTable().open(player);
        });
    }
}
