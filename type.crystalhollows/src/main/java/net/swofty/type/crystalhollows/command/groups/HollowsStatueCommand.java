package net.swofty.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.swofty.type.crystalhollows.command.HollowsSupport;
import net.swofty.type.crystalhollows.layout.NucleusStatueFile;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusConfig;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Stores the block you are looking at as a Nucleus crystal statue",
        labels = "statue",
        usage = "/hollows statue set <crystal>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsStatueCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<NucleusCrystal> crystal = ArgumentType.Enum("crystal", NucleusCrystal.class);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            NucleusCrystal chosen = context.get(crystal);
            Point block = HollowsSupport.targetBlock(player);
            NucleusStatueFile.set(NucleusConfig.FILE, chosen, block.blockX(), block.blockY(), block.blockZ());
            NucleusConfig.reload();
            player.sendMessage("<a>{} statue set to block <e>{} {} {}<a>.", chosen.coloured(), block.blockX(), block.blockY(), block.blockZ());
        }, ArgumentType.Literal("set"), crystal);
    }
}
