package dev.daweapon.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crystalhollows.command.HollowsSupport;
import dev.daweapon.type.crystalhollows.layout.HollowsAnchors;
import dev.daweapon.type.crystalhollows.layout.StructureCatalogue;
import dev.daweapon.type.crystalhollows.layout.StructureFile;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Stores your position as an anchor offset from the nearest candidate origin of a structure",
        labels = "anchor",
        usage = "/hollows anchor set <structure> <anchorName>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsAnchorCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord structure = HollowsSupport.structureArgument("structure");
        ArgumentWord anchor = HollowsSupport.suggesting("anchorName", () -> {
            return HollowsSupport.references().stream().map(HollowsAnchors.Ref::anchor).distinct().sorted().toList();
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String id = context.get(structure);
            String name = context.get(anchor);
            StructureFile.Entry entry = StructureFile.read(StructureCatalogue.FILE).find(id);
            if (entry == null) {
                player.sendMessage("<c>Unknown structure <e>{}<c>.", id);
                return;
            }
            Pos origin = entry.nearestCandidate(player.getPosition());
            if (origin == null) {
                player.sendMessage("<c>{} has no candidates yet. Stand at its origin and run <e>/hollows candidate add {}<c> first.", id, id);
                return;
            }
            Pos offset = HollowsSupport.offset(origin, player.getPosition());
            HollowsSupport.edit(file -> file.setAnchor(id, name, offset));
            player.sendMessage("<a>Anchor <e>{}/{}<a> set to offset <e>{} {} {}<a> from candidate <e>{} {} {}<a>.",
                    id, name, offset.x(), offset.y(), offset.z(), origin.x(), origin.y(), origin.z());
        }, ArgumentType.Literal("set"), structure, anchor);
    }
}
