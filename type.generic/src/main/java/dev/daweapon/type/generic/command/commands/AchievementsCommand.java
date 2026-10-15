package dev.daweapon.type.generic.command.commands;

import dev.daweapon.type.generic.achievement.gui.AchievementMenus;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

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
