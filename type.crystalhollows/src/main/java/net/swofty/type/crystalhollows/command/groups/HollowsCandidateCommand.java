package net.swofty.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.crystalhollows.command.HollowsSupport;
import net.swofty.type.crystalhollows.layout.StructureCatalogue;
import net.swofty.type.crystalhollows.layout.StructureFile;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Records, lists or clears the candidate origins of a structure",
        labels = "candidate",
        usage = "/hollows candidate <add|list|clear> <structure>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsCandidateCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord structure = HollowsSupport.structureArgument("structure");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String id = context.get(structure);
            if (unknown(player, id)) return;
            Pos origin = HollowsSupport.rounded(player.getPosition());
            HollowsSupport.edit(file -> file.addCandidate(id, origin));
            player.sendMessage("<a>Added candidate <e>{} {} {}<a> (yaw {}) to <e>{}<a> and reloaded the layout.",
                    origin.x(), origin.y(), origin.z(), origin.yaw(), id);
        }, ArgumentType.Literal("add"), structure);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            String id = context.get(structure);
            if (unknown(sender, id)) return;
            var candidates = StructureFile.read(StructureCatalogue.FILE).find(id).candidates();
            ((SkyBlockPlayer) sender).sendMessage("<a>{} has <e>{}<a> candidates.", id, candidates.size());
            for (int index = 0; index < candidates.size(); index++) {
                Pos at = candidates.get(index);
                ((SkyBlockPlayer) sender).sendMessage("<7>#{} <f>{} {} {} <8>yaw {}", index + 1, at.x(), at.y(), at.z(), at.yaw());
            }
        }, ArgumentType.Literal("list"), structure);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            String id = context.get(structure);
            if (unknown(sender, id)) return;
            HollowsSupport.edit(file -> file.clearCandidates(id));
            ((SkyBlockPlayer) sender).sendMessage("<a>Cleared every candidate of <e>{}<a> and reloaded the layout.", id);
        }, ArgumentType.Literal("clear"), structure);
    }

    private static boolean unknown(net.minestom.server.command.CommandSender sender, String id) {
        if (StructureFile.read(StructureCatalogue.FILE).find(id) != null) return false;
        ((SkyBlockPlayer) sender).sendMessage("<c>Unknown structure <e>{}<c>. Press Tab to see them all.", id);
        return true;
    }
}
