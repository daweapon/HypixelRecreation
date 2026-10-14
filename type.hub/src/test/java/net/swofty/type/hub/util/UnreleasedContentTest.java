package net.swofty.type.hub.util;

import net.swofty.commons.text.Text;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnreleasedContentTest {
    @Test
    void loreIsBlankLineThenMessage() {
        for (UnreleasedContent content : UnreleasedContent.values()) {
            List<Text> lore = content.loreLines();
            assertEquals(2, lore.size(), content.name());
            assertEquals(Text.empty(), lore.get(0));
            assertEquals(content.message(), lore.get(1));
        }
    }

    @Test
    void messagesAreDistinctPerContentType() {
        long distinct = java.util.Arrays.stream(UnreleasedContent.values()).map(UnreleasedContent::message).distinct().count();
        assertEquals(UnreleasedContent.values().length, distinct);
    }
}
