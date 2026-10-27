package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.hunting.HuntrapService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Collects the Shards from every Huntrap that has caught something",
        usage = "/collecthuntraps",
        permission = Rank.DEFAULT,
        labels = "collecthuntraps",
        allowsConsole = false)
public class CollectHuntrapsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (HuntrapService.collectAll(player) == 0) player.sendMessage("<c>None of your Huntraps have caught anything yet!");
        });
    }
}
