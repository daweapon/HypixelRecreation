package net.swofty.type.crystalhollows.command.groups;

import net.swofty.type.crystalhollows.layout.SchematicFiles;
import net.swofty.type.crystalhollows.layout.StructureCatalogue;
import net.swofty.type.crystalhollows.layout.StructureSpec;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@CommandParameters(description = "Lists the schematic files and which structures reference them",
        labels = "schematics",
        usage = "/hollows schematics",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsSchematicsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            List<StructureSpec> specs = StructureCatalogue.load();
            List<SchematicFiles.Reference> references = SchematicFiles.references(specs);
            Set<String> names = new TreeSet<>(SchematicFiles.listFiles(SchematicFiles.DIRECTORY));
            List<String> missing = references.stream().map(ref -> SchematicFiles.canonical(ref.file())).filter(name -> !names.contains(name)).distinct().toList();
            player.sendMessage("<a>{} schematic files in <e>{}<a>.", names.size(), SchematicFiles.DIRECTORY);
            for (String name : names) {
                SchematicFiles.Probe probe = SchematicFiles.probe(SchematicFiles.DIRECTORY, name);
                String detail = probe.readable()
                        ? probe.schematic().width() + "x" + probe.schematic().height() + "x" + probe.schematic().length()
                        + (probe.schematic().unknownBlocks() > 0 ? ", " + probe.schematic().unknownBlocks() + " unknown blocks" : "")
                        : "unreadable: " + probe.error();
                player.sendMessage("<f>{} <8>({}) <7>{}", name, detail, usedBy(references, name));
            }
            for (String name : missing) {
                player.sendMessage("<c>Missing {} <7>{}", name, usedBy(references, name));
            }
        });
    }

    private static String usedBy(List<SchematicFiles.Reference> references, String name) {
        List<String> users = references.stream().filter(ref -> SchematicFiles.canonical(ref.file()).equals(name))
                .map(ref -> ref.structure() + (ref.role() == SchematicFiles.Role.EMPTY_PATCH ? " (empty patch)" : "")).toList();
        return users.isEmpty() ? "unused" : "used by " + String.join(", ", users);
    }
}
