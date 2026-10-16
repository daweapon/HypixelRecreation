package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionNames;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.collection.GUICollectionItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Opens up a collections GUI",
        usage = "/viewcollection <collection>",
        permission = Rank.DEFAULT,
    labels = "vc viewcollection",
        allowsConsole = false)
public class ViewCollectionCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentItemType itemArgument = new ArgumentItemType("item");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final ItemType itemTypeLinker = CollectionNames.canonical(context.get(itemArgument));

            if (CollectionCategories.getCategory(itemTypeLinker) == null) {
                sender.sendMessage("<c>This item does not have a collection!");
                return;
            }

            ((SkyBlockPlayer) sender).openView(new GUICollectionItem(itemTypeLinker));
        }, itemArgument);
    }
}
