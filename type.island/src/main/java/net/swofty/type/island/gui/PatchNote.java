package net.swofty.type.island.gui;

import net.minestom.server.item.Material;
import net.swofty.commons.YamlFileUtils;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

public record PatchNote(String version, String name, String date, Material material, String title, String url, String book) {
    private static final File PATCH_NOTES_FILE = new File("./configuration/skyblock/patch_notes.yml");

    @SuppressWarnings("unchecked")
    public static List<PatchNote> load() {
        try {
            Map<String, Object> root = YamlFileUtils.loadYaml(PATCH_NOTES_FILE);
            List<Map<String, Object>> entries = (List<Map<String, Object>>) root.getOrDefault("patch_notes", List.of());
            return entries.stream().map(PatchNote::parse).toList();
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load patch notes");
            return List.of();
        }
    }

    public boolean hasBook() {
        return !book.isBlank();
    }

    public File bookFile() {
        return new File("./configuration/skyblock/patch_notes/" + book);
    }

    static PatchNote parse(Map<String, Object> entry) {
        Material material = Material.fromKey("minecraft:" + String.valueOf(entry.getOrDefault("material", "book")).toLowerCase());
        return new PatchNote(
                String.valueOf(entry.get("version")),
                String.valueOf(entry.getOrDefault("name", "SkyBlock v" + entry.get("version"))),
                String.valueOf(entry.getOrDefault("date", "")),
                material == null ? Material.BOOK : material,
                String.valueOf(entry.getOrDefault("title", "")),
                String.valueOf(entry.getOrDefault("url", "")),
                String.valueOf(entry.getOrDefault("book", ""))
        );
    }
}
