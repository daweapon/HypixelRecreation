package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.Wiki;

@CommandParameters(
    labels = "wikithis wikihand wikiinhand",
    description = "Shows page link of the item held towards the official Hypixel Wiki.",
    usage = "/wikithis",
    permission = Rank.DEFAULT,
    allowsConsole = false
)
public class WikiThisCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            Wiki.wikiThis(player);
        });
    }
}
