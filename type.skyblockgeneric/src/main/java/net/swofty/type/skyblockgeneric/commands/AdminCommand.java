package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.*;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.gui.inventories.admin.GUIAdmin;

@CommandParameters(labels="admin adminmenu",description="Open the staff administration menu",usage="/admin",permission=Rank.STAFF,allowsConsole=false)
public class AdminCommand extends HypixelCommand {
    @Override public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender,context) -> {
            if(!permissionCheck(sender))return;
            new GUIAdmin().open((HypixelPlayer)sender);
        });
    }
}
