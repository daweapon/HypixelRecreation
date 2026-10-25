package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.greatspook.GUIVargulShop;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Opens Vargul the Unearthed's shop during the Great Spook", usage = "/openvargulmenu",
        permission = Rank.DEFAULT, labels = "openvargulmenu", allowsConsole = false)
public class OpenVargulMenuCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!GreatSpookEvent.isActive()) {
                player.sendMessage("<c>Vargul only wakes during the Great Spook!");
                return;
            }
            player.openView(new GUIVargulShop());
        });
    }
}
