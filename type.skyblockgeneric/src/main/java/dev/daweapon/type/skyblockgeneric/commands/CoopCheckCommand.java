package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.gui.inventories.coop.GUICoopInviteSender;
import dev.daweapon.type.skyblockgeneric.gui.inventories.coop.GUICoopInviteTarget;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "cooperativecheck coopcheck",
        description = "Checks your pending co-op invite",
        usage = "/coopcheck",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopCheckCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());

            if (coop == null) {
                player.sendMessage("<c>You don't have any co-op invites!");
                return;
            }

            if (coop.isOriginator(player.getUuid()) && !coop.memberInvites().isEmpty()) {
                new GUICoopInviteSender(coop).open(player);
                return;
            }

            if (coop.members().contains(player.getUuid())) {
                player.sendMessage("<b>[Co-op] <c>You are already in a co-op!");
                return;
            }

            new GUICoopInviteTarget(coop).open(player);
        });
    }
}
