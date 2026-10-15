package dev.daweapon.type.generic.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.generic.command.commands.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CommandShortcutTest {
    @BeforeAll
    static void initializeServer() {
        MinecraftServer.init();
    }

    @Test
    void forwardsChatAndListShortcutsThroughTheTargetCommand() {
        var manager = MinecraftServer.getCommandManager();
        var received = new ArrayList<String>();
        var allowed = new java.util.concurrent.atomic.AtomicBoolean(true);
        var party = target("party", received);
        var guild = target("guild", received);
        party.setCondition((sender, input) -> allowed.get());
        guild.setCondition((sender, input) -> allowed.get());
        manager.register(party);
        manager.register(guild);
        List<HypixelCommand> shortcuts = List.of(new PartyChatCommand(), new GuildChatCommand(),
                new OfficerChatCommand(), new PartyListCommand(), new GuildListCommand());
        try {
            for (var shortcut : shortcuts) {
                shortcut.getCommand().setCondition((sender, input) -> true);
                manager.register(shortcut.getCommand());
            }
            for (String label : List.of("pc", "pchat", "gc", "gchat", "oc", "ochat")) {
                manager.execute(manager.getConsoleSender(), label + " hello two words");
            }
            manager.execute(manager.getConsoleSender(), "pl");
            manager.execute(manager.getConsoleSender(), "gl");
            assertEquals(List.of("party chat hello two words", "party chat hello two words",
                    "guild chat hello two words", "guild chat hello two words",
                    "guild officerchat hello two words", "guild officerchat hello two words",
                    "party list", "guild list"), received);

            received.clear();
            allowed.set(false);
            for (String input : List.of("pc denied", "gc denied", "oc denied", "pl", "gl")) {
                manager.execute(manager.getConsoleSender(), input);
            }
            assertEquals(List.of(), received);
        } finally {
            shortcuts.forEach(shortcut -> manager.unregister(shortcut.getCommand()));
            manager.unregister(party);
            manager.unregister(guild);
        }
    }

    @Test
    void messageAndChatAliasesResolveToTheirOriginalCommands() {
        var manager = MinecraftServer.getCommandManager();
        var message = new MessageCommand().getCommand();
        var chat = new ChatCommand().getCommand();
        manager.register(message);
        manager.register(chat);
        try {
            for (String label : List.of("msg", "message", "whisper", "w", "tell", "t", "m")) {
                assertSame(message, manager.getCommand(label));
            }
            assertSame(chat, manager.getCommand("ch"));
        } finally {
            manager.unregister(message);
            manager.unregister(chat);
        }
    }

    private static Command target(String name, List<String> received) {
        var command = new Command(name);
        var args = ArgumentType.StringArray("args");
        command.addSyntax((sender, context) -> received.add(name + " " + String.join(" ", context.get(args))), args);
        return command;
    }
}
