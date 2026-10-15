package dev.daweapon.type.skyblockgeneric.enchantment;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.EnchantmentCatalogue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentRenderTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    void sourcesAndLevelsDoNotThrowAnythingButMissingPlayer() {
        List<String> failures = new ArrayList<>();
        for (EnchantmentType type : EnchantmentType.values()) {
            try {
                type.getEnch().getLevelsToApply(null).minimumLevel();
                type.getEnch().getSources(null);
            } catch (NullPointerException ignored) {
            } catch (RuntimeException exception) {
                failures.add(type.name() + ": " + exception);
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void everyDescriptionRendersForEveryCataloguedLevel() {
        List<String> failures = new ArrayList<>();
        for (EnchantmentType type : EnchantmentType.values()) {
            CatalogueEntry entry = EnchantmentCatalogue.find(type.name());
            int max = entry == null ? 6 : entry.maxLevel();
            for (int level = type == EnchantmentType.VICIOUS ? 3 : 1; level <= max; level++) {
                try {
                    String description = type.getEnch().getDescription(level);
                    Text.of("<7><wrap:30>{}</wrap>", Text.parseLenient(description)).lines();
                    Text.parseLenient(type.getName());
                } catch (RuntimeException exception) {
                    failures.add(type.name() + " level " + level + ": " + exception);
                }
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }
}
