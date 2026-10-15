package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.GUIEnchantmentTable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
