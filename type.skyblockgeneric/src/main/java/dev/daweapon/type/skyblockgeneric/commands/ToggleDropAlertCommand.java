package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Toggles drop alert",
        usage = "/toggledropalert",
        permission = Rank.DEFAULT,
    labels = "dropalert toggledropalert",
        allowsConsole = false)
public class ToggleDropAlertCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            DatapointToggles.Toggles toggles = ((SkyBlockPlayer) sender).getToggles();

            toggles.inverse(DatapointToggles.Toggles.ToggleType.DISABLE_DROP_MESSAGES);

            sender.sendMessage(Text.of(toggles.get(DatapointToggles.Toggles.ToggleType.DISABLE_DROP_MESSAGES)
                    ? "<a>Drop alerts toggled <c>OFF<a>!" : "<a>Drop alerts toggled <a>ON<a>!"));
        });
    }
}
