package net.swofty.type.skyblockgeneric.commands;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.recipe.GUIRecipe;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
