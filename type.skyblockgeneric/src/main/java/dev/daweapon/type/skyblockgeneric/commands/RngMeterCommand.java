package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.experiments.GUIExperimentRngMeter;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "rngmeter experimentrngmeter",
        description = "Opens the Experimentation Table RNG Meter",
        usage = "/rngmeter",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class RngMeterCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            ((SkyBlockPlayer) sender).openView(new GUIExperimentRngMeter());
        });
    }
}
