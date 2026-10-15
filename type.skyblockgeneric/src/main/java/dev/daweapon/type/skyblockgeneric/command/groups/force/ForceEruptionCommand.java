package dev.daweapon.type.skyblockgeneric.command.groups.force;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoExplosivity;

@CommandParameters(description = "Starts a Blazing Volcano eruption on the Crimson Isle right now",
        labels = "eruption",
        usage = "/force eruption [explosivity]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceEruptionCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<VolcanoExplosivity> explosivityArgument = ArgumentType.Enum("explosivity", VolcanoExplosivity.class);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender) || !onCrimsonIsle(sender)) return;
            VolcanoEruptionService.forceEruption(null);
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender) || !onCrimsonIsle(sender)) return;
            VolcanoEruptionService.forceEruption(context.get(explosivityArgument));
        }, explosivityArgument);
    }

    private static boolean onCrimsonIsle(net.minestom.server.command.CommandSender sender) {
        if (HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_CRIMSON_ISLE) return true;
        sender.sendMessage("<c>The Blazing Volcano is on the Crimson Isle. Use this there.");
        return false;
    }
}
