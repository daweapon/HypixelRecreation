package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneIncomingCalls;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

@CommandParameters(description = "Pick up an incoming Abiphone call", usage = "/abiphonepickup <contact>", permission = Rank.DEFAULT,
        labels = "abiphonepickup", allowsConsole = false)
public class AbiphonePickUpCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord contactArg = ArgumentType.Word("contact");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!AbiphoneIncomingCalls.pickUp(player, context.get(contactArg))) player.sendMessage("<c>That call has already ended!");
        }, contactArg);
    }
}
