package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "coopclear",
        description = "Clears pending co-op invites",
        usage = "/coopclear",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopClearCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
            if (coop == null) {
                player.sendMessage("<b>[Co-op] <c>You do not have any pending co-op invites!");
                return;
            }

            if (coop.members().contains(player.getUuid())) {
                if (coop.memberInvites().isEmpty()) {
                    player.sendMessage("<b>[Co-op] <c>You do not have any pending co-op invites!");
                    return;
                }
                CoopDatabase.update(coop.coopUUID(), latest -> latest.memberInvites().clear());
                player.sendMessage("<b>[Co-op] <e>Cleared your pending co-op invites!");
                return;
            }

            CoopDatabase.update(coop.coopUUID(), latest -> latest.removeInvite(player.getUuid()));
            player.sendMessage("<b>[Co-op] <e>Cleared your pending co-op invite!");
        });
    }
}
