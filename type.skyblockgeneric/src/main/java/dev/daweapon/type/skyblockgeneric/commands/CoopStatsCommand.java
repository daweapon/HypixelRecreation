package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
            if (!CoopDatabase.isMember(coop, player.getUuid())) {
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
