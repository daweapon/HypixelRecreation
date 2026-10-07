package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.GUIPets;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "pets petsmenu petmenu viewpetsmenu",
        description = "Opens the Pets menu",
        usage = "/pets",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class PetsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            player.openView(new GUIPets(), GUIPets.createInitialState(player));
        });
    }
}
