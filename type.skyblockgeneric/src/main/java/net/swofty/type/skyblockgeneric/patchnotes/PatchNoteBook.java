package net.swofty.type.skyblockgeneric.patchnotes;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.swofty.commons.ChatUtility;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A server patch note written as a Minecraft book. The source is a YAML file of sections, each with a
 * heading and plain-text bullet lines; lines are word-wrapped to the book's width and flowed onto pages.
 */
public final class PatchNoteBook {
    static final int PAGE_WIDTH = 114;
    static final int PAGE_LINES = 14;
    private static final String BULLET = "• ";

    private final List<List<String>> pages = new ArrayList<>();
    private List<String> page = new ArrayList<>();

    private PatchNoteBook() {
    }

    public static Book load(File file) throws Exception {
        return build(YamlFileUtils.loadYaml(file)).toBook();
    }

    @SuppressWarnings("unchecked")
    static PatchNoteBook build(Map<String, Object> source) {
        PatchNoteBook book = new PatchNoteBook();
        book.titlePage(String.valueOf(source.get("title")), String.valueOf(source.getOrDefault("date", "")),
                (List<String>) source.getOrDefault("intro", List.of()));
        for (Map<String, Object> section : (List<Map<String, Object>>) source.getOrDefault("sections", List.of())) {
            book.section(String.valueOf(section.get("heading")), String.valueOf(section.getOrDefault("colour", "<4>")),
                    (List<String>) section.getOrDefault("lines", List.of()));
        }
        book.newPage();
        return book;
    }

    List<List<String>> pages() {
        return pages;
    }

    private Book toBook() {
        Book.Builder builder = Book.builder().title(Component.text("Patch Notes")).author(Component.text("Jerry"));
        for (List<String> lines : pages) {
            builder.addPage(page(lines));
        }
        return builder.build();
    }

    static Component page(List<String> lines) {
        return Text.parse(String.join("\n", lines.stream().map(line -> "<r>" + line).toList())).asComponent();
    }

    private void titlePage(String title, String date, List<String> intro) {
        for (String line : wrap(title, true)) add("<4><l>" + line);
        if (!date.isBlank()) add("<8>" + date);
        for (String paragraph : intro) {
            add("");
            for (String line : wrap(paragraph, false)) add("<0>" + line);
        }
        newPage();
    }

    private void section(String heading, String colour, List<String> lines) {
        List<String> headingLines = wrap(heading, true);
        if (page.size() + headingLines.size() + 2 > PAGE_LINES) newPage();
        if (!page.isEmpty()) add("");
        for (String line : headingLines) add(colour + "<l>" + line);
        for (String bullet : lines) {
            List<String> wrapped = wrap(BULLET + bullet, false);
            if (page.size() + wrapped.size() > PAGE_LINES && wrapped.size() <= PAGE_LINES) newPage();
            for (int i = 0; i < wrapped.size(); i++) {
                String line = wrapped.get(i);
                add(i == 0 ? "<8>" + BULLET + "<0>" + line.substring(BULLET.length()) : "<0>" + line);
            }
        }
    }

    private void add(String line) {
        if (page.size() == PAGE_LINES) newPage();
        if (page.isEmpty() && line.isEmpty()) return;
        page.add(line);
    }

    private void newPage() {
        if (page.isEmpty()) return;
        pages.add(page);
        page = new ArrayList<>();
    }

    static List<String> wrap(String text, boolean bold) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split(" +")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (width(candidate, bold) <= PAGE_WIDTH) {
                line.setLength(0);
                line.append(candidate);
                continue;
            }
            if (!line.isEmpty()) lines.add(line.toString());
            line.setLength(0);
            line.append(word);
            while (width(line.toString(), bold) > PAGE_WIDTH) {
                int cut = line.length() - 1;
                while (cut > 1 && width(line.substring(0, cut), bold) > PAGE_WIDTH) cut--;
                lines.add(line.substring(0, cut));
                line.delete(0, cut);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }

    static int width(String text, boolean bold) {
        return ChatUtility.FontInfo.getLength(bold ? "§l" + text : text);
    }
}
