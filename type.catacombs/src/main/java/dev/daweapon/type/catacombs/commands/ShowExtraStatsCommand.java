package dev.daweapon.type.catacombs.commands;

import dev.daweapon.type.catacombs.run.CatacombsRun;
import dev.daweapon.type.catacombs.run.CatacombsRuns;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "showextrastats",
        description = "Shows your stats for the Catacombs run that just ended",
        usage = "/showextrastats",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class ShowExtraStatsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            CatacombsRun run = CatacombsRuns.of(player.getUuid());
            if (run == null || !run.showExtraStats(player)) player.sendMessage("<c>There are no stats to show yet!");
        });
    }
}
