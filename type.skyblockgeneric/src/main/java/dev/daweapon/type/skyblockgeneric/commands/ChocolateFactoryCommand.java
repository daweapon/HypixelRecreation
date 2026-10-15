package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.rabbits.GUIChocolateFactory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;

@CommandParameters(labels = "chocolatefactory cf factory",
        description = "Opens the Chocolate Factory menu",
        usage = "/chocolatefactory",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class ChocolateFactoryCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            player.openView(new GUIChocolateFactory()).refreshEvery(Duration.ofSeconds(1));
        });
    }
}