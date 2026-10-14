package net.swofty.type.skyblockgeneric.command.groups.max;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "crystals",
        description = "Gives you every Crystal Nucleus crystal, unplaced",
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
                    ? "<a>You already hold every Crystal, unplaced."
                    : "<a>You received <e>" + given + " <a>Crystal" + (given == 1 ? "" : "s") + ", all unplaced.");
        });
    }
}
