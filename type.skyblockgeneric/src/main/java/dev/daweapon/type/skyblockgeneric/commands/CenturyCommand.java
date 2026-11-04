package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.century.raffle.RaffleMenus;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Opens the Incredible Raffle Box of the Raffle of the Century",
        usage = "/century",
        permission = Rank.DEFAULT,
        labels = "century centuryrafflebox",
        allowsConsole = false)
public class CenturyCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            RaffleMenus.openBox((SkyBlockPlayer) sender);
        });
    }
}
