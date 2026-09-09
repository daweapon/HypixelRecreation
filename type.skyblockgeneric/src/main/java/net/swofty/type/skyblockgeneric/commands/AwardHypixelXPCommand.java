package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "awardskyblockxp giveskyblockxp",
        description = "Gives yourself skyblock xp",
        usage = "/giveskyblockxp <cause> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class AwardHypixelXPCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString causeArgument = ArgumentType.String("cause");
        causeArgument.setSuggestionCallback((_, _, suggestion) -> {
            SkyBlockLevelCause.getCauses().forEach((causeKey, causeAbstr) -> {
                suggest(suggestion, causeKey, "<6>{}", causeAbstr.xpReward());
            });
        });
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            awardXp((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(causeArgument));
        }, causeArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            awardXp((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(causeArgument));
        }, causeArgument, targetArgument);
    }

    private void awardXp(SkyBlockPlayer sender, SkyBlockPlayer target, String causeString) {
        SkyBlockLevelCauseAbstr cause = SkyBlockLevelCause.getCause(causeString);

        if (cause == null) {
            sender.sendMessage("<c>Invalid cause.");
            SkyBlockLevelCause.getCauses().forEach((causeKey, causeAbstr) -> {
                sender.sendMessage(Text.of("<7>- <a>{}", causeKey));
            });
            return;
        }

        target.getSkyBlockExperience().addExperience(cause);

        if (target == sender) {
            sender.sendMessage("<a>Awarded {} with {} xp.", cause, cause.xpReward());
        } else {
            target.sendMessage("<a>You were awarded {} with {} xp.", cause, cause.xpReward());
            sender.sendMessage("<a>Awarded <e>{}<a> {} with {} xp.", target.getUsername(), cause, cause.xpReward());
        }
    }
}
