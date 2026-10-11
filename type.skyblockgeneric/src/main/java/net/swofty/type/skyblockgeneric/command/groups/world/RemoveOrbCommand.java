package net.swofty.type.skyblockgeneric.command.groups.world;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.data.crystals.CrystalCatalog;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

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
