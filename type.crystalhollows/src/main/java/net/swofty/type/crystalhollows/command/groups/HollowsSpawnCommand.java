package net.swofty.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.crystalhollows.command.HollowsSupport;
import net.swofty.type.crystalhollows.layout.HollowsWorld;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Sets the lobby spawn to your position",
        labels = "spawn",
        usage = "/hollows spawn set",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsSpawnCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            Pos spawn = HollowsSupport.rounded(player.getPosition());
            HollowsWorld.get().withSpawn(spawn).write(HollowsWorld.FILE);
            HollowsWorld.reload();
            player.sendMessage("<a>Crystal Hollows spawn set to <e>{} {} {}<a> (yaw {}). New joiners use it immediately.", spawn.x(), spawn.y(), spawn.z(), spawn.yaw());
        }, ArgumentType.Literal("set"));
    }
}
