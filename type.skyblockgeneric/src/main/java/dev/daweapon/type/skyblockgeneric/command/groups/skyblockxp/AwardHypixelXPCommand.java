package dev.daweapon.type.skyblockgeneric.command.groups.skyblockxp;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelCause;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "cause",
        description = "Gives yourself skyblock xp",
        usage = "/skyblockxp cause <cause> [player]",
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
