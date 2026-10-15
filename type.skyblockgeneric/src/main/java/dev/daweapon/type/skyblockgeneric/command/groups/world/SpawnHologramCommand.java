package dev.daweapon.type.skyblockgeneric.command.groups.world;

import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.entity.hologram.HologramEntity;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "hologram",
        description = "Rank command",
        usage = "/world hologram <text>",
        permission = Rank.STAFF,
    allowsConsole = false
)
public class SpawnHologramCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentStringArray nameArgument = ArgumentType.StringArray("name");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String[] nameArray = context.get(nameArgument);
            String name = String.join(" ", nameArray);
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            Entity entity = new HologramEntity(name);
            entity.setAutoViewable(true);
            entity.setInstance(HypixelConst.getInstanceContainer(), player.getPosition());
        }, nameArgument);
    }
}
