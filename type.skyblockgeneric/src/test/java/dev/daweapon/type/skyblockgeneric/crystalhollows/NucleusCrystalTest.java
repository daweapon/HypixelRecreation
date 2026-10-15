package dev.daweapon.type.skyblockgeneric.crystalhollows;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import dev.daweapon.commons.text.Text;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NucleusCrystalTest {
    @Test
    void colouredNameRendersWithoutRawTagsWhenUsedAsAnArgument() {
        for (NucleusCrystal crystal : NucleusCrystal.values()) {
            String plain = PlainTextComponentSerializer.plainText()
                    .serialize(Text.of("<a>You placed the {}<a>!", crystal.coloured()).asComponent());
            assertEquals("You placed the " + crystal.getDisplayName() + " Crystal!", plain);
            assertFalse(plain.contains("<"));
        }
    }

    @Test
    void everyCrystalHasAnItemModelAndLegacyColour() {
        for (NucleusCrystal crystal : NucleusCrystal.values()) {
            assertEquals("hypixel_skyblock:item/uncategorized/" + crystal.name().toLowerCase() + "_crystal", crystal.itemModel());
            assertEquals(2, crystal.getLegacyColour().length());
        }
    }
}
