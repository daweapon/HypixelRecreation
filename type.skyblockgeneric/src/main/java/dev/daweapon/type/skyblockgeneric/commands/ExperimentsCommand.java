package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.experiments.GUIExperimentationTable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "experiments",
        description = "Opens the Experimentation Table menu remotely",
        usage = "/experiments",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ExperimentsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetService.fire((SkyBlockPlayer) sender, dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetTrigger.EXPERIMENTATION_TABLE);
            ((SkyBlockPlayer) sender).openView(new GUIExperimentationTable());
        });
    }
}
