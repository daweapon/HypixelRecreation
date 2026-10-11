package net.swofty.type.skyblockgeneric.command.groups.set;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "xp",
        description = "Sets your experience levels",
        usage = "/set xp <level> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class XpCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentInteger levelArgument = ArgumentType.Integer("level");
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            int level = context.get(levelArgument);
            setLevel((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, level);
        }, levelArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            int level = context.get(levelArgument);
            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            setLevel((SkyBlockPlayer) sender, (SkyBlockPlayer) target, level);
        }, levelArgument, targetArgument);
    }

    private void setLevel(SkyBlockPlayer sender, SkyBlockPlayer target, int level) {
        target.setLevel(level);

        if (target == sender) return;
        target.sendMessage("<a>Your experience level was set to <e>{}<a>.", level);
        sender.sendMessage("<a>Set <e>{}<a>'s experience level to <e>{}<a>.", target.getUsername(), level);
    }
}
