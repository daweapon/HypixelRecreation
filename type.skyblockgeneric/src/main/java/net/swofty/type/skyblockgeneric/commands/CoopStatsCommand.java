package net.swofty.type.skyblockgeneric.commands;

import net.swofty.commons.StringUtility;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "coopstats",
        description = "Shows each co-op member's mining/farming contribution",
        usage = "/coopstats",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopStatsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
            if (coop == null) {
                player.sendMessage("<b>[Co-op] <c>You are not part of a co-op!");
                return;
            }

            player.sendMessage("<b>----------------------------------------");
            player.sendMessage("<e>Co-op Stats");
            player.sendMessage("");
            coop.members().stream()
                    .sorted((a, b) -> Long.compare(coop.getContribution(b), coop.getContribution(a)))
                    .forEach(member -> player.sendMessage("<6>{} <8>- <a>{} <7>contributed",
                            HypixelPlayer.getRawName(member), StringUtility.commaify(coop.getContribution(member))));
            player.sendMessage("<b>----------------------------------------");
        });
    }
}
