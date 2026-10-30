package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.island.pad.TeleporterPill;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "teleports",
        description = "Opens the Teleport Pads menu on your Private Island",
        usage = "/teleports",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class TeleportsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, context) -> {
            if (!permissionCheck(sender)) return;
            TeleporterPill.openMenu((SkyBlockPlayer) sender);
        });
    }
}
