package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.hotf.GUIHotf;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Opens the Heart of the Forest",
    usage = "/hotf",
    permission = Rank.DEFAULT,
    labels = "hotf heartoftheforest",
    allowsConsole = false)
public class HotfCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!HotfService.isUnlocked(player)) {
                player.sendMessage("<c>Talk to Hina in Tangleburg's Path on Galatea to unlock your Heart of the Forest!");
                return;
            }
            player.openView(new GUIHotf());
        });
    }
}
