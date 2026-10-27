package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.hunting.GUIHuntingToolkit;
import dev.daweapon.type.skyblockgeneric.hunting.HuntingToolkit;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Opens the Hunting Toolkit",
        usage = "/huntingtoolkit",
        permission = Rank.DEFAULT,
        labels = "huntingtoolkit",
        allowsConsole = false)
public class HuntingToolkitCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!HuntingToolkit.isUnlocked(player)) {
                player.sendMessage("<c>You haven't unlocked the Hunting Toolkit yet!");
                return;
            }
            player.openView(new GUIHuntingToolkit());
        });
    }
}
