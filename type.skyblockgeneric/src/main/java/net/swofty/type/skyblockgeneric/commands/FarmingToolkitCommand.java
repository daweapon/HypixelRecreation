package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.gui.inventories.garden.GUIFarmingToolkit;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
