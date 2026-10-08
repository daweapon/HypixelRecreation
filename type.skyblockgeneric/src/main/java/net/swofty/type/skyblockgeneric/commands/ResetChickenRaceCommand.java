package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.chickenrace.ChickenRaceProgress;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Resets Chicken Race cluckpoints and the talked-to-Frosty flag",
        labels = "resetchickenrace",
        usage = "/resetchickenrace [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ResetChickenRaceCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            reset((SkyBlockPlayer) sender, (SkyBlockPlayer) sender);
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            reset((SkyBlockPlayer) sender, (SkyBlockPlayer) target);
        }, targetArgument);
    }

    private void reset(SkyBlockPlayer sender, SkyBlockPlayer target) {
        ChickenRaceProgress.setClaimed(target, 0);
        target.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FROSTY, false);
        sender.sendMessage("<a>Reset <e>{}<a>'s Chicken Race progress.", target.getUsername());
    }
}
