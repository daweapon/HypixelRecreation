package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.corpse.GUIFrozenCorpseMilestones;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "viewfrozencorpsemilestones corpsemilestones",
        description = "Opens the Frozen Corpse Milestones menu",
        usage = "/viewfrozencorpsemilestones",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class ViewFrozenCorpseMilestonesCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            ((SkyBlockPlayer) sender).openView(new GUIFrozenCorpseMilestones());
        });
    }
}
