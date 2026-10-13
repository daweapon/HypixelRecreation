package net.swofty.type.crystalhollows.command.groups;

import net.swofty.type.crystalhollows.command.HollowsDebugMarkers;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Toggles particle markers on structure origins, candidates and anchors",
        labels = "debug",
        usage = "/hollows debug",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsDebugCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            boolean on = HollowsDebugMarkers.toggle(player);
            player.sendMessage(on ? "<a>Markers on: <c>flame<a> = placed origin, <b>spark<a> = unplaced candidate, <a>green<a> = anchor."
                    : "<7>Markers off.");
        });
    }
}
