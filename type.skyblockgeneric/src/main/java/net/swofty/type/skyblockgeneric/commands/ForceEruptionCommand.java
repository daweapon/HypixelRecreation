package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoExplosivity;

@CommandParameters(description = "Starts a Blazing Volcano eruption on the Crimson Isle right now",
        labels = "forceeruption",
        usage = "/forceeruption [explosivity]",
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
