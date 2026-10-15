package dev.daweapon.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import dev.daweapon.type.crystalhollows.command.HollowsSupport;
import dev.daweapon.type.crystalhollows.layout.CrystalHollowsLayout;
import dev.daweapon.type.crystalhollows.layout.SchematicFiles;
import dev.daweapon.type.crystalhollows.layout.StructureCatalogue;
import dev.daweapon.type.crystalhollows.layout.StructurePaster;
import dev.daweapon.type.crystalhollows.layout.StructurePlacement;
import dev.daweapon.type.crystalhollows.layout.StructureSpec;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.schematic.SchematicPaster;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Pastes a structure's schematic at its placement or at your position",
        labels = "paste",
        usage = "/hollows paste <structure> [here]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsPasteCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord structure = HollowsSupport.structureArgument("structure");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            paste((SkyBlockPlayer) sender, context.get(structure), false);
        }, ArgumentType.Literal("paste"), structure);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            paste((SkyBlockPlayer) sender, context.get(structure), true);
        }, ArgumentType.Literal("paste"), structure, ArgumentType.Literal("here"));
    }

    private static void paste(SkyBlockPlayer player, String id, boolean here) {
        StructureSpec spec = StructureCatalogue.load().stream().filter(s -> s.id().equals(id)).findFirst().orElse(null);
        if (spec == null) {
            player.sendMessage("<c>Unknown structure <e>{}<c>. Press Tab to see them all.", id);
            return;
        }
        if (spec.schematic() == null) {
            player.sendMessage("<c>{} has no schematic set in structures.yml.", id);
            return;
        }
        StructurePlacement placement = CrystalHollowsLayout.placement(id);
        if (!here && placement == null) {
            player.sendMessage("<c>{} is not placed in this layout. Use <e>/hollows paste {} here<c>.", id, id);
            return;
        }
        Pos at = here ? player.getPosition() : placement.origin();
        Vec origin = new Vec(at.blockX(), at.blockY(), at.blockZ());
        int rotation = here ? 0 : StructurePaster.rotationFor(spec, CrystalHollowsLayout.seed());
        StructurePaster.pasteFile(player.getInstance(), SchematicPaster.NEXT_TICK, SchematicFiles.DIRECTORY, spec.schematic(), origin, rotation)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        player.sendMessage("<c>Paste failed: {}", error.getMessage());
                        return;
                    }
                    player.sendMessage("<a>Pasted <e>{}<a> blocks of {} at <e>{} {} {}<a> ({} unknown skipped, {} outside the world).",
                            result.placed(), id, origin.blockX(), origin.blockY(), origin.blockZ(), result.unknown(), result.outOfWorld());
                });
        player.sendMessage("<7>Pasting {} at {} {} {} (rotation {})...", id, origin.blockX(), origin.blockY(), origin.blockZ(), rotation);
    }
}
