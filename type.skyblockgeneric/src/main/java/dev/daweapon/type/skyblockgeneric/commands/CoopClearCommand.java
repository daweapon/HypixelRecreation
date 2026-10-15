package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
