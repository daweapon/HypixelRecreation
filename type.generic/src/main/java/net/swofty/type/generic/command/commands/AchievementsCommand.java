package net.swofty.type.generic.command.commands;

import net.swofty.type.generic.achievement.gui.AchievementMenus;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "achievements achievement",
    description = "Opens the achievements menu",
    usage = "/achievements",
    permission = Rank.DEFAULT,
    allowsConsole = false)
public class AchievementsCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof HypixelPlayer player)) return;

            AchievementMenus.open(player);
        });
    }
}
