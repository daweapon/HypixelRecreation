package net.swofty.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.crystalhollows.chest.StructureChests;
import net.swofty.type.crystalhollows.command.HollowsSupport;
import net.swofty.type.crystalhollows.layout.StructureCatalogue;
import net.swofty.type.crystalhollows.layout.StructureFile;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Stores the chest you are looking at as the anchor of a configured structure chest",
        labels = "chest",
        usage = "/hollows chest set <id>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsChestCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord chest = HollowsSupport.suggesting("id", () -> StructureChests.specs().stream().map(StructureChests.ChestSpec::id).toList());

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String id = context.get(chest);
            StructureChests.ChestSpec spec = StructureChests.specs().stream().filter(entry -> entry.id().equals(id)).findFirst().orElse(null);
            if (spec == null) {
                player.sendMessage("<c>Unknown chest <e>{}<c>. Press Tab to see them all.", id);
                return;
            }
            StructureFile.Entry entry = StructureFile.read(StructureCatalogue.FILE).find(spec.structure());
            Pos origin = entry == null ? null : entry.nearestCandidate(player.getPosition());
            if (origin == null) {
                player.sendMessage("<c>{} has no candidates yet. Run <e>/hollows candidate add {}<c> first.", spec.structure(), spec.structure());
                return;
            }
            Point block = HollowsSupport.targetBlock(player);
            Pos centre = new Pos(block.blockX() + 0.5, block.blockY() + 0.5, block.blockZ() + 0.5);
            Pos offset = HollowsSupport.offset(origin, centre);
            HollowsSupport.edit(file -> file.setAnchor(spec.structure(), spec.anchor(), offset));
            player.sendMessage("<a>Chest <e>{}<a> set to block <e>{} {} {}<a> (anchor {}/{}).",
                    id, block.blockX(), block.blockY(), block.blockZ(), spec.structure(), spec.anchor());
        }, ArgumentType.Literal("set"), chest);
    }
}
