package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalLeaderboards;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Claims your rewards from the last Carnival's leaderboards",
        usage = "/carnivalrewards",
        permission = Rank.DEFAULT,
        labels = "carnivalrewards",
        allowsConsole = false)
public class CarnivalRewardsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            CarnivalLeaderboards.claim((SkyBlockPlayer) sender);
        });
    }
}
