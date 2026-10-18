package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.settings.GUISettings;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "viewsettings opengamesettings",
        description = "Opens the SkyBlock settings menu",
        usage = "/viewsettings",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class ViewSettingsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            ((SkyBlockPlayer) sender).openView(new GUISettings());
        });
    }
}
