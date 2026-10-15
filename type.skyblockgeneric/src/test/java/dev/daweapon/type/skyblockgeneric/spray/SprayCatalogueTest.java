package dev.daweapon.type.skyblockgeneric.spray;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SprayCatalogueTest {
    private static final Path DIRECTORY = Path.of("../configuration/skyblock");
    private static final Map<String, Spray> SPRAYS = SprayCatalogue.parse(DIRECTORY.resolve("sprays.yml"));

    @Test
    void catalogueListsEveryHypixelSpray() {
        assertEquals(132, SPRAYS.size());
        assertEquals(21, SPRAYS.values().stream().filter(Spray::isUnlocked).count());
        assertTrue(SPRAYS.values().stream().filter(spray -> !spray.isUnlocked())
                .allMatch(spray -> spray.source().unavailableReason() != null));
    }

    @Test
    void unlockedSpraysHaveInkAndA128PixelPicture() throws IOException {
        for (Spray spray : SPRAYS.values()) {
            if (!spray.isUnlocked()) continue;
            assertFalse(spray.ink().isEmpty(), spray.id());
            BufferedImage image = ImageIO.read(DIRECTORY.resolve("sprays").resolve(spray.image()).toFile());
            assertNotNull(image, spray.id());
            assertEquals(128, image.getWidth(), spray.id());
            assertEquals(128, image.getHeight(), spray.id());
        }
    }

    @Test
    void arrowSpraysOfferTheirDirections() {
        assertEquals(4, SprayDirection.offeredBy(SPRAYS.get("FARMING_ARROW")).size());
        assertEquals(8, SprayDirection.offeredBy(SPRAYS.get("RED_ARROW")).size());
        assertEquals(List.of(SprayInk.LIGHT_GRAY_DYE, SprayInk.ROSE_RED), List.copyOf(SPRAYS.get("RED_ARROW").ink().keySet()));
    }
}
