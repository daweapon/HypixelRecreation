package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AbiphoneUnlocks {
    private static final File FILE = new File("./configuration/skyblock/abiphone_unlocks.yml");
    private static final Map<String, String> CONTACT_BY_NPC = new HashMap<>();

    private AbiphoneUnlocks() {
    }

    public static String contactForNpc(String npcClassName) {
        return CONTACT_BY_NPC.get(npcClassName);
    }

    public static void registerAll() {
        for (ConfiguredContact.Definition definition : load(FILE)) {
            AbiphoneContact entry = AbiphoneContactCatalogue.byId(definition.id()).orElseThrow();
            AbiphoneRegistry.registerContact(new ConfiguredContact(definition, entry));
            definition.npcs().forEach(npc -> CONTACT_BY_NPC.put(npc, definition.id()));
        }
    }

    @SuppressWarnings("unchecked")
    public static List<ConfiguredContact.Definition> load(File file) {
        try {
            List<ConfiguredContact.Definition> loaded = new ArrayList<>();
            for (Map<String, Object> entry : (List<Map<String, Object>>) YamlFileUtils.loadYaml(file).get("contacts")) {
                List<ConfiguredContact.Stage> stages = new ArrayList<>();
                for (Map<String, Object> stage : (List<Map<String, Object>>) entry.getOrDefault("stages", List.of())) {
                    List<ConfiguredContact.Requirement> items = new ArrayList<>();
                    for (Map<String, Object> item : (List<Map<String, Object>>) stage.getOrDefault("items", List.of())) {
                        items.add(new ConfiguredContact.Requirement(ItemType.valueOf(String.valueOf(item.get("item"))), ((Number) item.get("amount")).intValue()));
                    }
                    Object check = stage.get("check");
                    stages.add(new ConfiguredContact.Stage(
                            String.valueOf(stage.get("label")),
                            lines(stage.get("intro")), lines(stage.get("missing")), lines(stage.get("accepted")), items,
                            check == null ? null : AbiphoneCheck.valueOf(String.valueOf(check)),
                            stage.get("phrase") == null ? null : String.valueOf(stage.get("phrase"))));
                }
                loaded.add(new ConfiguredContact.Definition(
                        String.valueOf(entry.get("id")), lines(entry.get("npc")), (String) entry.get("head"), List.copyOf(stages), lines(entry.get("given"))));
            }
            return List.copyOf(loaded);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> lines(Object value) {
        return value == null ? List.of() : List.copyOf((List<String>) value);
    }
}
