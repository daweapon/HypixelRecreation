package dev.daweapon.type.skyblockgeneric.command.groups.debug;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import dev.daweapon.type.generic.user.categories.Rank;
import org.tinylog.Logger;

@CommandParameters(labels = "configids",
        description = "Prints out the item IDs from the config into the console",
        usage = "/debug configids",
        permission = Rank.STAFF,
        allowsConsole = false)
public class PrintConfigIdsCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            sender.sendMessage("<a>Available Item IDs:");
            for (String id : ConfigurableSkyBlockItem.getIDs()) {
                sender.sendMessage(Text.of("<7>{}", id));
                Logger.info(id);
            }
        });
    }
}
