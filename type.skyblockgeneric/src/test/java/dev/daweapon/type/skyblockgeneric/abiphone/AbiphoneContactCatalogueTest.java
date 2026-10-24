package dev.daweapon.type.skyblockgeneric.abiphone;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbiphoneContactCatalogueTest {
    private static final List<AbiphoneContact> CONTACTS = AbiphoneContactCatalogue.load(new File("../configuration/skyblock/abiphone_contacts.yml"));

    @Test
    void listsAllEightyFiveContactsOnce() {
        assertEquals(AbiphoneContactXP.MAX_CONTACTS, CONTACTS.size());
        assertEquals(85, CONTACTS.stream().map(AbiphoneContact::id).distinct().count());
    }

    @Test
    void everyAliasBelongsToOneContact() {
        Set<String> seen = new HashSet<>();
        for (AbiphoneContact contact : CONTACTS) {
            assertTrue(contact.aliases().contains(contact.id()), contact.id());
            for (String alias : contact.aliases()) assertTrue(seen.add(alias), "duplicate alias " + alias);
        }
    }

    @Test
    void everyContactHasALocationAndDescription() {
        for (AbiphoneContact contact : CONTACTS) {
            assertTrue(!contact.location().isBlank() && !contact.location().equals("?"), contact.id());
            assertTrue(!contact.description().isBlank(), contact.id());
        }
    }

    @Test
    void registeredContactClassesAreAllInTheCatalogue() throws Exception {
        Set<String> ids = new HashSet<>(CONTACTS.stream().map(AbiphoneContact::id).toList());
        java.nio.file.Path root = java.nio.file.Path.of("src/main/java");
        try (var files = java.nio.file.Files.walk(root.resolve("dev/daweapon/type/skyblockgeneric/abiphone/impl"))) {
            for (java.nio.file.Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String source = java.nio.file.Files.readString(file);
                var matcher = java.util.regex.Pattern.compile("super\\(\"([a-z_]+)\"").matcher(source);
                if (matcher.find()) assertTrue(ids.contains(matcher.group(1)), file + " " + matcher.group(1));
            }
        }
    }

    @Test
    void callStatsCountAndRememberTheLastCall() {
        String stats = AbiphoneContactData.recordCall("", "alda", 100);
        stats = AbiphoneContactData.recordCall(stats, "alda", 250);
        stats = AbiphoneContactData.recordCall(stats, "builder", 300);
        assertEquals(2, AbiphoneContactData.callStats(stats).get("alda").count());
        assertEquals(250, AbiphoneContactData.callStats(stats).get("alda").lastCalled());
        assertEquals(1, AbiphoneContactData.callStats(stats).get("builder").count());
    }

    @Test
    void doNotDisturbToggles() {
        String ids = AbiphoneContactData.toggleDoNotDisturb("", "alda");
        assertTrue(AbiphoneContactData.doNotDisturb(ids).contains("alda"));
        assertTrue(AbiphoneContactData.doNotDisturb(AbiphoneContactData.toggleDoNotDisturb(ids, "alda")).isEmpty());
    }
}
