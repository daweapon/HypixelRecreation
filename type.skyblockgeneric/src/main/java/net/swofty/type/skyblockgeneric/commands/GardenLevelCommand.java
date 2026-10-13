package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.garden.GardenConfig;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Sets your Garden level exactly",
        usage = "/gardenlevel <level>",
        permission = Rank.STAFF,
        labels = "gardenlevel setgardenlevel",
        allowsConsole = false)
public class GardenLevelCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentInteger levelArgument = ArgumentType.Integer("level");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            GardenConfig config = GardenConfig.get();
            int level = context.get(levelArgument);
            if (level < 1 || level > config.maxLevel()) {
                player.sendMessage("<c>The level must be between 1 and " + config.maxLevel() + "<c>.");
                return;
            }
            GardenProfile profile = Gardens.profile(player);
            profile.setGardenXp(config.level(level).cumulativeXp());
            Gardens.save(player, profile);
            player.sendMessage("<a>Your Garden level is now " + level + "<a>.");
        }, levelArgument);
    }
}
