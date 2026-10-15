package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.GUICreative;
import dev.daweapon.type.skyblockgeneric.gui.inventories.centauri.GUICentauriItemCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "itemlist e",
        description = "Opens the item categories, or searches every category",
        usage = "/e [lookup]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ItemListCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            ((SkyBlockPlayer) sender).openView(new GUICentauriItemCategories(), new GUICentauriItemCategories.State());
        });

        ArgumentString lookup = new ArgumentString("lookup");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String lookupValue = context.get(lookup);
            ((SkyBlockPlayer) sender).openView(new GUICreative(), GUICreative.createInitialState(lookupValue, 0));
        }, lookup);
    }
}
