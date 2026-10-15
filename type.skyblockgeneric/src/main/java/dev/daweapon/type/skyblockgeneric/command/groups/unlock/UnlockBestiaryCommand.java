package dev.daweapon.type.skyblockgeneric.command.groups.unlock;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "bestiary",
        description = "Unlocks the Bestiary menu without needing to speak to Bramass Beastslayer",
        usage = "/unlock bestiary [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class UnlockBestiaryCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            unlock((SkyBlockPlayer) sender, (SkyBlockPlayer) sender);
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (!(target instanceof SkyBlockPlayer skyBlockTarget)) {
                sender.sendMessage(Text.of("<c>Could not find that player."));
                return;
            }

            unlock((SkyBlockPlayer) sender, skyBlockTarget);
        }, targetArgument);
    }

    private void unlock(SkyBlockPlayer sender, SkyBlockPlayer target) {
        target.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BRAMASS_BEASTSLAYER, true);
        target.sendMessage("<a>The Bestiary is now unlocked! Find it in your Combat Skill menu.");
        if (target != sender) {
            sender.sendMessage(Text.of("<a>Unlocked the Bestiary for <e>{}<a>.", target.getUsername()));
        }
    }
}
