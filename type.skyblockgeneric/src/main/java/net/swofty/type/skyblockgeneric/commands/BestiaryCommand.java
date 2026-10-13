package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.GUIBestiary;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "bestiary be",
        description = "Opens the Bestiary menu",
        usage = "/bestiary",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class BestiaryCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BRAMASS_BEASTSLAYER)) {
                player.sendMessage("<c>You have not unlocked the Bestiary yet! Speak to Bramass Beastslayer.");
                return;
            }
            player.openView(new GUIBestiary());
        });
    }
}
