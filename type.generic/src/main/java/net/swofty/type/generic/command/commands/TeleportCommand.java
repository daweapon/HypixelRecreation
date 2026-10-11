package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.swofty.type.generic.command.CommandGroup;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.groups.tp.TeleportToNPCCommand;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "tp teleport",
        description = "Teleports you to a player, to coordinates or to an NPC",
        usage = "/tp <player> | /tp <x> <y> <z> | /tp npc <npc>",
        permission = Rank.STAFF,
        allowsConsole = false)
// Staff command to teleport to a player or to given coordinates.
public class TeleportCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEntity entityArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            final Player target = context.get(entityArgument).findFirstPlayer(sender);

            if (target == null) {
                player.sendMessage("<c>Couldn't find a player by the name of <e>{}<c>.", context.getRaw(entityArgument));
                return;
            }
            player.teleport(target.getPosition());
        }, entityArgument);

        ArgumentInteger xArgument = ArgumentType.Integer("x");
        ArgumentInteger yArgument = ArgumentType.Integer("y");
        ArgumentInteger zArgument = ArgumentType.Integer("z");
        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;
            int x = context.get(xArgument);
            int y = context.get(yArgument);
            int z = context.get(zArgument);
            player.teleport(player.getPosition().withX(x).withY(y).withZ(z));
        }, xArgument, yArgument, zArgument);

        command.addSubcommand(new TeleportToNPCCommand().getCommand());
    }

    @Override
    public List<Command> legacyCommands() {
        return CommandGroup.legacyCommands(getCommand(), Map.of("tpnpc", "tp npc", "teleportnpc", "tp npc"));
    }
}