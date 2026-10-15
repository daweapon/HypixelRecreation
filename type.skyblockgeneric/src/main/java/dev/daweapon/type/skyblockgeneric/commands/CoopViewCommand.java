package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.mongodb.ProfilesDatabase;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

@CommandParameters(labels = "coopview",
        description = "Views a player's co-op",
        usage = "/coopview <player>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopViewCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString targetArgument = ArgumentType.String("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String targetName = context.get(targetArgument);
            UUID targetId;
            try {
                targetId = ProfilesDatabase.fetchUUID(targetName);
            } catch (Exception exception) {
                player.sendMessage("<b>[Co-op] <c>That player could not be found!");
                return;
            }

            CoopDatabase.Coop coop = CoopDatabase.getFromMember(targetId);
            if (coop == null || !coop.members().contains(targetId)) {
                player.sendMessage("<b>[Co-op] <c>{} is not in a co-op!", targetName);
                return;
            }

            player.sendMessage("<b>----------------------------------------");
            player.sendMessage("<b>[Co-op] <e>{}'s co-op ({} members)", targetName, coop.members().size());
            for (UUID member : coop.members()) {
                player.sendMessage(Text.of("<7>- ").append(SkyBlockPlayer.getDisplayName(member)));
            }
            player.sendMessage("<b>----------------------------------------");
        }, targetArgument);
    }
}
