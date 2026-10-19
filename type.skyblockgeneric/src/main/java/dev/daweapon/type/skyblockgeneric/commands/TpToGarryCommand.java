package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "tptogarry",
        description = "Teleports you to Old Man Garry in the Dwarven Village",
        usage = "/tptogarry",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class TpToGarryCommand extends HypixelCommand {
    public static final Pos GARRY = new Pos(5.5, 200, -108.5, 0, 0);

    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_DWARVEN_MINES) {
                player.sendMessage("<c>Old Man Garry is in the Dwarven Mines.");
                return;
            }
            player.teleport(GARRY);
        });
    }
}
