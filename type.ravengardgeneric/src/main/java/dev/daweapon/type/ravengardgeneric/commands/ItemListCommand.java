package dev.daweapon.type.ravengardgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.gui.v2.ViewNavigator;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.ravengardgeneric.gui.GUIItemList;
import dev.daweapon.type.ravengardgeneric.user.RavengardPlayer;

@CommandParameters(
        labels = "items e",
        description = "Browse every implemented Ravengard item",
        usage = "/items [search]",
        permission = Rank.STAFF,
        allowsConsole = false)
// Staff command that opens a browsable/searchable GUI listing every implemented Ravengard item.
public class ItemListCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            RavengardPlayer player = (RavengardPlayer) sender;
            ViewNavigator.get(player).push(new GUIItemList(null, 0));
        });

        ArgumentString lookup = new ArgumentString("lookup");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            RavengardPlayer player = (RavengardPlayer) sender;
            ViewNavigator.get(player).push(new GUIItemList(context.get(lookup), 0));
        }, lookup);
    }
}
