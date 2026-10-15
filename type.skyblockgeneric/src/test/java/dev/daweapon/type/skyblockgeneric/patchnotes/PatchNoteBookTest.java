package dev.daweapon.type.skyblockgeneric.patchnotes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.text.Text;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatchNoteBookTest {
    private static final File DIRECTORY = new File("../configuration/skyblock/patch_notes");
    private static final int MAX_PAGES = 100;

    @Test
    void everyPatchNoteFitsTheBook() throws Exception {
        File[] files = Objects.requireNonNull(DIRECTORY.listFiles((_, name) -> name.endsWith(".yml")));
        assertTrue(files.length > 0);
        for (File file : files) {
            PatchNoteBook book = PatchNoteBook.build(YamlFileUtils.loadYaml(file));
            assertFalse(book.pages().isEmpty(), file.getName());
            assertTrue(book.pages().size() <= MAX_PAGES, file.getName() + " has too many pages");
            for (List<String> page : book.pages()) {
                assertTrue(page.size() <= PatchNoteBook.PAGE_LINES, file.getName() + " page overflows: " + page);
                for (String line : page) {
                    String plain = Text.parse(line).plain();
                    assertTrue(PatchNoteBook.width(plain, line.contains("<l>")) <= PatchNoteBook.PAGE_WIDTH,
                            file.getName() + " line too wide: " + plain);
                }
                PatchNoteBook.page(page);
            }
        }
    }

    @Test
    void wrapKeepsEveryWordAndRespectsTheWidth() {
        String text = "Catch all 18 Trophy Fish, fillet them into Magmafish and claim Trophy Fisher rank rewards.";
        List<String> lines = PatchNoteBook.wrap(text, false);
        assertTrue(lines.size() > 1);
        assertEquals(text, String.join(" ", lines));
        lines.forEach(line -> assertTrue(PatchNoteBook.width(line, false) <= PatchNoteBook.PAGE_WIDTH));
    }

    @Test
    void boldHeadingDoesNotLeakIntoTheNextLine() {
        Component page = PatchNoteBook.page(List.of("<4><l>Heading", "<0>Body"));
        assertFalse(isBold(page, "Body", false), "body text inherited the heading's bold");
        assertTrue(isBold(page, "Heading", false));
    }

    @Test
    void sectionsStartWithTheirHeading() {
        PatchNoteBook book = PatchNoteBook.build(Map.of(
                "title", "Test Update",
                "sections", List.of(Map.of("heading", "Section", "lines", List.of("one", "two")))));
        assertEquals(2, book.pages().size());
        assertTrue(book.pages().get(1).getFirst().contains("Section"));
    }

    private static boolean isBold(Component component, String text, boolean inherited) {
        TextDecoration.State state = component.decoration(TextDecoration.BOLD);
        boolean bold = state == TextDecoration.State.NOT_SET ? inherited : state == TextDecoration.State.TRUE;
        if (component instanceof TextComponent textComponent && textComponent.content().contains(text)) return bold;
        for (Component child : component.children()) {
            if (containsText(child, text)) return isBold(child, text, bold);
        }
        return false;
    }

    private static boolean containsText(Component component, String text) {
        if (component instanceof TextComponent textComponent && textComponent.content().contains(text)) return true;
        return component.children().stream().anyMatch(child -> containsText(child, text));
    }
}
