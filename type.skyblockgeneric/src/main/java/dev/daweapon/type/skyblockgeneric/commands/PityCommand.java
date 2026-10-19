package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.hoppity.RabbitRoller;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "pity",
        description = "Shows your Chocolate Rabbit pity progress",
        usage = "/pity",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class PityCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
            player.sendMessage("<d><l>RABBIT PITY");
            player.sendMessage("<7>Every <d>{}th <7>rabbit is a <d>MYTHIC<7>: <a>{}<7>/<a>{}{}", RabbitRoller.MYTHIC_PITY,
                    data.getMythicPity(), RabbitRoller.MYTHIC_PITY, locked(data, RabbitRarity.MYTHIC));
            player.sendMessage("<7>Every <b>{}th <7>rabbit is a <b>DIVINE<7>: <a>{}<7>/<a>{}{}", RabbitRoller.DIVINE_PITY,
                    data.getDivinePity(), RabbitRoller.DIVINE_PITY, locked(data, RabbitRarity.DIVINE));
        });
    }

    private static String locked(ChocolateFactoryData data, RabbitRarity rarity) {
        int needed = RabbitRoller.unlockLevel(rarity);
        return data.getFactoryLevel() >= needed ? "" : " <8>(needs Chocolate Factory " + needed + ")";
    }
}
