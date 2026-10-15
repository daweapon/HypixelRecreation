package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.auction.GUIAuctionBrowser;
import dev.daweapon.type.skyblockgeneric.gui.inventories.auction.GUIAuctionViewItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

@CommandParameters(labels = "auctionview ahview",
        description = "Views an auction",
        usage = "/ahview <uuid>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class AhViewCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentUUID uuid = new ArgumentUUID("uuid");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            UUID ah = context.get(uuid);

            new GUIAuctionViewItem(ah, new GUIAuctionBrowser()).open((SkyBlockPlayer) sender);
        }, uuid);
    }
}
