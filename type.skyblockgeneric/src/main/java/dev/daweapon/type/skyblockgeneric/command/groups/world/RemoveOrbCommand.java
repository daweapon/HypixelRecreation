package dev.daweapon.type.skyblockgeneric.command.groups.world;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.skyblockgeneric.data.crystals.CrystalCatalog;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "deleteorb",
        description = "Deletes orbs at the player's location.",
        usage = "/world deleteorb",
        permission = Rank.STAFF,
        allowsConsole = false)
public class RemoveOrbCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            CrystalCatalog.removeCrystals(((SkyBlockPlayer) sender).getPosition(), 1.5);
        });
    }
}
