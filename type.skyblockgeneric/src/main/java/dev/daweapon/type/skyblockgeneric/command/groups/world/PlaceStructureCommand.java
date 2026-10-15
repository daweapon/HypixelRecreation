package dev.daweapon.type.skyblockgeneric.command.groups.world;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.instance.SharedInstance;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.skyblockgeneric.structure.structures.IslandPortal;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "structure",
        description = "Places a test debug structure",
        usage = "/world structure",
        permission = Rank.STAFF,
        allowsConsole = false)
public class PlaceStructureCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentInteger rotation = ArgumentType.Integer("rotation");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            IslandPortal portal = new IslandPortal(context.get(rotation),
                    player.getPosition().blockX(),
                    player.getPosition().blockY(),
                    player.getPosition().blockZ());

            portal.setType(IslandPortal.PortalType.HUB);
            portal.build((SharedInstance) player.getInstance());
        }, rotation);
    }
}
