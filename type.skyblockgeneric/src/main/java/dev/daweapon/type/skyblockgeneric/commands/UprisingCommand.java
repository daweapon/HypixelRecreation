package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.dante.DanteUprising;
import dev.daweapon.type.skyblockgeneric.dante.UprisingGear;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "uprising",
        description = "Stages or ends the Uprising boss fight against Dante and hands out Resistance gear",
        usage = "/uprising <start|stop|gear [general]>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class UprisingCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!DanteUprising.start(player)) {
                player.sendMessage("<c>The Uprising is already underway. Use <e>/uprising stop <c>to end it.");
            }
        }, ArgumentType.Literal("start"));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            DanteUprising.stop();
            sender.sendMessage("<a>The Uprising has been called off and its mobs removed.");
        }, ArgumentType.Literal("stop"));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            UprisingGear.give((SkyBlockPlayer) sender, UprisingGear.RESISTANCE_KIT);
        }, ArgumentType.Literal("gear"));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            UprisingGear.give((SkyBlockPlayer) sender, UprisingGear.GENERALS_KIT);
        }, ArgumentType.Literal("gear"), ArgumentType.Literal("general"));
    }
}
