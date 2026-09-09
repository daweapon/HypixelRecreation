package net.swofty.type.generic.command;

import net.minestom.server.MinecraftServer;
import net.swofty.type.generic.command.commands.HelpCommand;
import net.swofty.type.generic.command.commands.MessageCommand;
import net.swofty.type.generic.command.help.HelpCatalog;
import net.swofty.type.generic.user.categories.Rank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class HelpCatalogTest {
    @BeforeAll
    static void initializeServer() {
        MinecraftServer.init();
    }

    @Test
    void filtersStaffCommandsAndResolvesAliasesWithoutDuplicateRows() {
        var commands = List.of(new HelpCommand().getCommand(), new MessageCommand().getCommand(),
                new StaffCommand().getCommand());
        var player = HelpCatalog.entries(new java.util.ArrayList<>(commands), Rank.DEFAULT, false);
        assertTrue(HelpCatalog.find(player, "secret").isEmpty());
        assertTrue(HelpCatalog.find(player, "find").isEmpty());
        assertTrue(HelpCatalog.find(player, "party").isEmpty());
        assertEquals("msg", HelpCatalog.find(player, "/W").orElseThrow().name());
        assertEquals(1, player.stream().filter(entry -> entry.labels().contains("msg")).count());
        assertEquals(player.stream().map(HelpCatalog.Entry::name).sorted().toList(),
                player.stream().map(HelpCatalog.Entry::name).toList());
        var staff = HelpCatalog.entries(new java.util.ArrayList<>(commands), Rank.STAFF, false);
        assertTrue(HelpCatalog.find(staff, "secret").isPresent());
        assertTrue(HelpCatalog.find(staff, "find").isPresent());
    }

    @Test
    void authenticationCommandsFollowConfiguration() {
        assertTrue(HelpCatalog.find(HelpCatalog.entries(List.of(), Rank.DEFAULT, false), "login").isEmpty());
        var enabled = HelpCatalog.entries(List.of(), Rank.DEFAULT, true);
        assertTrue(HelpCatalog.find(enabled, "login").isPresent());
        assertTrue(HelpCatalog.find(enabled, "register").isPresent());
    }

    @Test
    void pagesCoverEveryEntryOnceAndRejectInvalidPages() {
        var entries = IntStream.range(0, 17).mapToObj(i -> new HelpCatalog.Entry("command" + i,
                List.of("command" + i), "/command" + i, "Example", Rank.DEFAULT)).toList();
        assertEquals(3, HelpCatalog.pageCount(entries));
        assertEquals(List.of(8, 8, 1), IntStream.rangeClosed(1, 3)
                .mapToObj(page -> HelpCatalog.page(entries, page).size()).toList());
        assertEquals(entries, IntStream.rangeClosed(1, 3)
                .boxed().flatMap(page -> HelpCatalog.page(entries, page).stream()).toList());
        for (int invalid : List.of(-1, 0, 4, Integer.MAX_VALUE)) {
            assertThrows(IllegalArgumentException.class, () -> HelpCatalog.page(entries, invalid));
        }
        assertEquals(List.of(), HelpCatalog.page(List.of(), 1));
    }

    @CommandParameters(labels = "secret", usage = "/secret", description = "Staff only",
            permission = Rank.STAFF, allowsConsole = false)
    static class StaffCommand extends HypixelCommand {
        @Override
        public void registerUsage(MinestomCommand command) {}
    }
}
