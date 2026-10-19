package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.miningevent.MithrilGourmandEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "tptodonexpresso",
        description = "Teleports you to Don Expresso while the Mithril Gourmand event runs",
        usage = "/tptodonexpresso",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class TpToDonExpressoCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_DWARVEN_MINES || MithrilGourmandEvent.active() == null) {
                player.sendMessage("<c>Don Expresso isn't here right now.");
                return;
            }
            player.teleport(MithrilGourmandEvent.donPosition().add(1.5, 0, 0));
        });
    }
}
