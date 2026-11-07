package dev.daweapon.type.catacombs.tomioka;

import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TomiokaConfigTest {
    private static final Path FILE = Path.of("..", "configuration", "skyblock", "dungeons", "tomioka.json");

    @Test
    void sellsTheThreeCapturedBooksAtTheCapturedPrices() throws Exception {
        Assumptions.assumeTrue(Files.exists(FILE));
        TomiokaConfig config = TomiokaConfig.load(FILE);
        assertEquals(List.of(
                new TomiokaConfig.Book(EnchantmentType.SHARPNESS, 6, 9_700_000),
                new TomiokaConfig.Book(EnchantmentType.SCAVENGER, 5, 14_550_000),
                new TomiokaConfig.Book(EnchantmentType.PROTECTION, 6, 3_880_000)), config.stock());
        assertEquals(List.of("Tomioka", "<e><l>CLICK"), config.nameLines());
        assertNotNull(config.skin().signature());
    }
}
