package net.swofty.type.skyblockgeneric.gui.inventories.admin;
import net.swofty.type.generic.command.*;
import net.swofty.type.generic.user.categories.Rank;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AdminCatalogTest {
    @CommandParameters(labels="ban tempban",description="Ban",usage="/ban <player> <reason>",permission=Rank.STAFF,allowsConsole=false)
    static class Ban extends HypixelCommand { public void registerUsage(MinestomCommand command) {} }
    @CommandParameters(labels="hello",description="Hello",usage="/hello",permission=Rank.DEFAULT,allowsConsole=false)
    static class Hello extends HypixelCommand { public void registerUsage(MinestomCommand command) {} }
    @CommandParameters(labels="admin",description="Admin",usage="/admin",permission=Rank.STAFF,allowsConsole=false)
    static class Admin extends HypixelCommand { public void registerUsage(MinestomCommand command) {} }
    @Test void nonStaffCannotDiscoverActions() {assertTrue(AdminCatalog.actions(List.of(new Ban().getCommand()),Rank.DEFAULT).isEmpty());}
    @Test void onlyRegisteredStaffActionsAppearAndUseDocumentCategories() {
        var actions=AdminCatalog.actions(List.of(new Ban().getCommand(),new Hello().getCommand(),new Admin().getCommand()),Rank.STAFF);
        assertEquals(1,actions.size());assertEquals("ban",actions.getFirst().command().name());
        assertEquals(AdminCatalog.Category.MODERATION,actions.getFirst().details().category());
        assertTrue(actions.getFirst().command().labels().contains("tempban"));
    }
    @Test void argumentsStayOneCommandAndPreserveQuotedValues() {
        assertEquals("ban player reason here",AdminCatalog.commandLine("ban"," player reason here "));
        assertEquals("editnbt key \"some value\"",AdminCatalog.commandLine("editnbt","key \"some value\""));
        assertThrows(IllegalArgumentException.class,()->AdminCatalog.commandLine("ban","player\nwipe"));
        assertThrows(IllegalArgumentException.class,()->AdminCatalog.commandLine("ban wipe",""));
        assertEquals("ban",AdminCatalog.commandLine("ban",null));
    }
}
