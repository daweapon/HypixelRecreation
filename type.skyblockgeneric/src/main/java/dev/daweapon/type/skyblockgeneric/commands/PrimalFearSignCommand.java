package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.greatspook.mob.CommitmentPhobiaMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

@CommandParameters(labels = "primalfearsign",
        description = "Signs a Commitment Phobia's paper",
        usage = "/primalfearsign <paper>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class PrimalFearSignCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString paper = new ArgumentString("paper");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof SkyBlockPlayer player)) return;
            UUID token;
            try {
                token = UUID.fromString(context.get(paper));
            } catch (IllegalArgumentException invalid) {
                return;
            }
            CommitmentPhobiaMob.sign(player, token);
        }, paper);
    }
}
