package dev.daweapon.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.crystalhollows.command.HollowsSupport;
import dev.daweapon.type.crystalhollows.layout.CrystalHollowsLayout;
import dev.daweapon.type.crystalhollows.layout.StructurePaster;
import dev.daweapon.type.crystalhollows.layout.StructurePlacement;
import dev.daweapon.type.skyblockgeneric.schematic.SchematicPaster;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

@CommandParameters(description = "Shows the current layout, or regenerates it",
        labels = "layout",
        usage = "/hollows layout [regenerate [seed]]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsLayoutCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        var regenerate = ArgumentType.Literal("regenerate");
        var seed = ArgumentType.Long("seed");

        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            ((SkyBlockPlayer) sender).sendMessage("<a>Layout seed <e>{}<a>, <e>{}<a> structures placed.", CrystalHollowsLayout.seed(), CrystalHollowsLayout.placements().size());
            for (StructurePlacement placement : CrystalHollowsLayout.placements().values()) {
                ((SkyBlockPlayer) sender).sendMessage("<7>{} <8>at <f>{} {} {} <8>({} anchors)", placement.spec().id(),
                        placement.origin().blockX(), placement.origin().blockY(), placement.origin().blockZ(), placement.anchors().size());
            }
        });

        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            regenerate(sender, ThreadLocalRandom.current().nextLong());
        }, regenerate);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            regenerate(sender, context.get(seed));
        }, regenerate, seed);
    }

    private static void regenerate(net.minestom.server.command.CommandSender sender, long seed) {
        HollowsSupport.reloadLayout(seed);
        var instance = dev.daweapon.type.generic.HypixelConst.getInstanceContainer();
        if (instance != null) {
            StructurePaster.apply(instance, SchematicPaster.NEXT_TICK).whenComplete((report, error) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) sender;
                if (error != null) {
                    player.sendMessage("<c>Structure paste failed: {}", error.getMessage());
                    return;
                }
                player.sendMessage("<a>Pasted <e>{}<a> structures, erased <e>{}<a> sites, <e>{}<a> failures.",
                        report.pasted().size(), report.erased().size(), report.failures().size());
            });
        }
        ((SkyBlockPlayer) sender).sendMessage("<a>Layout regenerated with seed <e>{}<a>: <e>{}<a> structures placed.", seed, CrystalHollowsLayout.placements().size());
    }
}
