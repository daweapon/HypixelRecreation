package dev.daweapon.type.skyblockgeneric.command.groups.hand;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.skyblockgeneric.item.ItemAttributeHandler;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "recomb",
        description = "Recombobulates the item in the players hand",
        usage = "/hand recomb",
        permission = Rank.STAFF,
        allowsConsole = false)
public class RecombobulateCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            ((SkyBlockPlayer) sender).updateItem(PlayerItemOrigin.MAIN_HAND, (item) -> {
                ItemAttributeHandler itemAttributeHandler = item.getAttributeHandler();
                if (itemAttributeHandler.isPet()) {
                    sender.sendMessage("<c>Pets cannot be recombobulated!");
                    return;
                }
                itemAttributeHandler.setRecombobulated(!itemAttributeHandler.isRecombobulated());
                sender.sendMessage(Text.of("<a>Recombobulated: <d>{}", itemAttributeHandler.isRecombobulated()));
            });
        });
    }
}