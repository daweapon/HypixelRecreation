package net.swofty.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.crystalhollows.command.HollowsSupport;
import net.swofty.type.crystalhollows.layout.CrystalHollowsLayout;
import net.swofty.type.crystalhollows.layout.StructurePlacement;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

@CommandParameters(description = "Teleports to a placed structure or one of its anchors",
        labels = "tp",
        usage = "/hollows tp <structure|structure/anchor>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsTpCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord target = HollowsSupport.suggesting("target", HollowsTpCommand::targets);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String input = context.get(target);
            String[] parts = input.split("/", 2);
            Pos destination = parts.length == 2 ? CrystalHollowsLayout.anchor(parts[0], parts[1]) : CrystalHollowsLayout.origin(parts[0]);
            if (destination == null) {
                player.sendMessage("<c>{} is not placed or has no such anchor. Run <e>/hollows layout<c> to see what is placed.", input);
                return;
            }
            player.teleport(destination);
            player.sendMessage("<a>Teleported to <e>{}<a>.", input);
        }, target);
    }

    private static List<String> targets() {
        List<String> targets = new ArrayList<>();
        for (StructurePlacement placement : CrystalHollowsLayout.placements().values()) {
            targets.add(placement.spec().id());
            placement.anchors().keySet().forEach(anchor -> targets.add(placement.spec().id() + "/" + anchor));
        }
        return targets;
    }
}
