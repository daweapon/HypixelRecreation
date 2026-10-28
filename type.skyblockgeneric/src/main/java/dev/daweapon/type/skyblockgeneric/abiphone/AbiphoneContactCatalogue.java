package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.commons.YamlFileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class AbiphoneContactCatalogue {
    private static final File FILE = new File("./configuration/skyblock/abiphone_contacts.yml");
    private static List<AbiphoneContact> contacts;

    private AbiphoneContactCatalogue() {
    }

    public static synchronized List<AbiphoneContact> all() {
        if (contacts == null) contacts = load(FILE);
        return contacts;
    }

    public static Optional<AbiphoneContact> byId(String id) {
        return all().stream().filter(contact -> contact.id().equalsIgnoreCase(id)).findFirst();
    }

    public static Optional<AbiphoneContact> byAlias(String alias) {
        String key = alias.toLowerCase(Locale.ROOT);
        return all().stream().filter(contact -> contact.aliases().contains(key)).findFirst();
    }

    @SuppressWarnings("unchecked")
    public static List<AbiphoneContact> load(File file) {
        try {
            List<AbiphoneContact> loaded = new ArrayList<>();
            for (Map<String, Object> entry : (List<Map<String, Object>>) YamlFileUtils.loadYaml(file).get("contacts")) {
                loaded.add(new AbiphoneContact(
                        String.valueOf(entry.get("id")),
                        String.valueOf(entry.get("name")),
                        AbiphoneContact.Difficulty.valueOf(String.valueOf(entry.get("difficulty"))),
                        String.valueOf(entry.get("location")),
                        String.valueOf(entry.get("description")),
                        String.valueOf(entry.getOrDefault("requirement", "")),
                        String.valueOf(entry.getOrDefault("usage", "")),
                        List.copyOf((List<String>) entry.get("aliases")),
                        String.valueOf(entry.getOrDefault("head", ""))));
            }
            return List.copyOf(loaded);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
