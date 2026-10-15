package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.hotm.GUIHotm;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Opens the Heart of the Mountain",
    usage = "/hotm",
    permission = Rank.DEFAULT,
    labels = "hotm heartofthemountain",
    allowsConsole = false)
public class HotmCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;

            ((SkyBlockPlayer) sender).openView(new GUIHotm());
        });
    }
}
