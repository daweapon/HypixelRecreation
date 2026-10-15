package dev.daweapon.type.crystalhollows.command.groups;

import dev.daweapon.type.crystalhollows.command.HollowsDebugMarkers;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
