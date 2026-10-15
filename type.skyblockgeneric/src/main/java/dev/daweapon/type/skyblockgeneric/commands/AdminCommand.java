package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.*;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.admin.GUIAdmin;

@CommandParameters(labels="admin adminmenu",description="Open the staff administration menu",usage="/admin",permission=Rank.STAFF,allowsConsole=false)
public class AdminCommand extends HypixelCommand {
    @Override public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender,context) -> {
            if(!permissionCheck(sender))return;
            new GUIAdmin().open((HypixelPlayer)sender);
        });
    }
}
