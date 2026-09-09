package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Private co-op chat channel (real Hypixel's /coopchat, /cchat, /cc) - delivers to every online
// member regardless of which server they're currently on. SendMessagePushProtocol only works from
// backend microservices (service.party/service.guild), not game-server commands like this one - the
// coop module's own ProxyPlayer-based fan-out (already used by CoopCommand/CoopAddCommand) is what
// actually resolves a member's real backend server and delivers to them.
@CommandParameters(labels = "coopchat cchat cc",
        description = "Sends a message to your co-op",
        usage = "/coopchat <message>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopChatCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentStringArray messageArg = ArgumentType.StringArray("message");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String message = String.join(" ", context.get(messageArg));

            CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
            if (coop == null || !coop.members().contains(player.getUuid())) {
                player.sendMessage("<b>[Co-op] <c>You are not in a co-op!");
                return;
            }

            Text formatted = Text.of("<b>Co-op > <e>{}<gray>: <f>{}", player.getFullDisplayName(), message);
            coop.getMembersAsProxyPlayerSet().asProxyPlayers().forEach(member -> member.sendMessage(formatted));
        }, messageArg);
    }
}
