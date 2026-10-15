package dev.daweapon.type.skyblockgeneric.command.groups.max;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "crystals",
        description = "Marks every Crystal Nucleus crystal as found, unplaced",
        usage = "/max crystals",
        permission = Rank.STAFF,
        allowsConsole = false)
public class MaxCrystalsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            int given = CrystalNucleus.grantAllUnplaced(player);
            player.sendMessage(given == 0
                    ? "<a>Every Crystal is already found, unplaced."
                    : "<a>You found <e>" + given + " <a>Crystal" + (given == 1 ? "" : "s") + ", all unplaced.");
        });
    }
}
