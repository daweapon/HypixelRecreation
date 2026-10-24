package dev.daweapon.type.skyblockgeneric.abiphone;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbiphoneUnlocksTest {
    private static final List<ConfiguredContact.Definition> UNLOCKS = AbiphoneUnlocks.load(new File("../configuration/skyblock/abiphone_unlocks.yml"));

    @Test
    void everyUnlockIsACatalogueContact() {
        Set<String> catalogue = new HashSet<>();
        AbiphoneContactCatalogue.load(new File("../configuration/skyblock/abiphone_contacts.yml")).forEach(contact -> catalogue.add(contact.id()));
        Set<String> seen = new HashSet<>();
        for (ConfiguredContact.Definition definition : UNLOCKS) {
            assertTrue(catalogue.contains(definition.id()), definition.id());
            assertTrue(seen.add(definition.id()), "duplicate " + definition.id());
        }
    }

    @Test
    void everyStageHasARequirementAndDialogue() {
        for (ConfiguredContact.Definition definition : UNLOCKS) {
            assertFalse(definition.given().isEmpty(), definition.id());
            for (ConfiguredContact.Stage stage : definition.stages()) {
                boolean hasRequirement = !stage.items().isEmpty() || stage.check() != null || stage.phrase() != null;
                assertTrue(hasRequirement, definition.id() + " stage has no requirement");
                assertFalse(stage.missing().isEmpty(), definition.id() + " has no missing line");
            }
        }
    }

    @Test
    void npcClassesAreNotSharedBetweenContacts() {
        Set<String> classes = new HashSet<>();
        for (ConfiguredContact.Definition definition : UNLOCKS) {
            for (String npc : definition.npcs()) assertTrue(classes.add(npc), npc);
        }
    }

    @Test
    void everyUnlockHasACallBinding() throws Exception {
        String source = java.nio.file.Files.readString(java.nio.file.Path.of("../loader/src/main/java/dev/daweapon/loader/AbiphoneCallBindings.java"));
        for (ConfiguredContact.Definition definition : UNLOCKS) {
            assertTrue(source.contains("register(\"" + definition.id() + "\""), definition.id());
        }
    }

    @Test
    void holdsTheThirtyOneHubAndAreaContacts() {
        assertEquals(31, UNLOCKS.size());
    }
}
