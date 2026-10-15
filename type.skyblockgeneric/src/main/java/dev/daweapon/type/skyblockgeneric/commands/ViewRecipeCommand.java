package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.recipe.GUIRecipe;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Opens up a recipe GUI",
        usage = "/viewrecipe <recipe>",
        permission = Rank.DEFAULT,
    labels = "vr viewrecipe",
        allowsConsole = false)
public class ViewRecipeCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentItemType itemArgument = new ArgumentItemType("item");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final ItemType item = context.get(itemArgument);
            ((SkyBlockPlayer) sender).openView(new GUIRecipe(item));
        }, itemArgument);
    }
}
