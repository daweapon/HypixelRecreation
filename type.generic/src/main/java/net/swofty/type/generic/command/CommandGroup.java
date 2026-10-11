package net.swofty.type.generic.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class CommandGroup extends HypixelCommand {
    protected abstract List<HypixelCommand> subcommands();

    protected abstract Map<String, String> legacyLabels();

    @Override
    public void registerUsage(MinestomCommand command) {
        subcommands().forEach(subcommand -> command.addSubcommand(subcommand.getCommand()));
    }

    @Override
    public List<Command> legacyCommands() {
        return legacyCommands(getCommand(), legacyLabels());
    }

    public static List<Command> legacyCommands(Command target, Map<String, String> legacyLabels) {
        List<Command> commands = new ArrayList<>();
        legacyLabels.forEach((label, forwardedPrefix) -> commands.add(new LegacyCommand(label, forwardedPrefix, target)));
        return commands;
    }

    public static void extend(String groupName, List<HypixelCommand> subcommands, Map<String, String> legacyLabels) {
        var manager = MinecraftServer.getCommandManager();
        Command group = manager.getCommand(groupName);
        if (group == null) throw new IllegalStateException("Command group " + groupName + " is not registered");
        subcommands.forEach(subcommand -> group.addSubcommand(subcommand.getCommand()));
        legacyCommands(group, legacyLabels).forEach(manager::register);
    }
}
