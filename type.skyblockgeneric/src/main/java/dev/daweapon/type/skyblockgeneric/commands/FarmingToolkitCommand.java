package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.gui.inventories.garden.GUIFarmingToolkit;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Opens the Farming Toolkit",
        usage = "/farmingtoolkit",
        permission = Rank.DEFAULT,
        labels = "farmingtoolkit",
        allowsConsole = false)
public class FarmingToolkitCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!Gardens.profile(player).toolkit().isUnlocked()) {
                player.sendMessage("<c>You haven't unlocked the Farming Toolkit yet!");
                return;
            }
            player.openView(new GUIFarmingToolkit());
        });
    }
}
