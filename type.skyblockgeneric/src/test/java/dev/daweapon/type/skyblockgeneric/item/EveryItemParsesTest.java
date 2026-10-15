package dev.daweapon.type.skyblockgeneric.item;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EveryItemParsesTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyConfiguredItemParses() throws IOException {
        List<String> failures = new ArrayList<>();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) {
                    Map<String, Object> item = (Map<String, Object>) entry;
                    try {
                        if (ItemConfigParser.parseItem(item) == null) failures.add(item.get("id") + " (" + file.getFileName() + ")");
                    } catch (RuntimeException exception) {
                        failures.add(item.get("id") + " (" + file.getFileName() + "): " + exception.getMessage());
                    }
                }
            }
        }
        assertTrue(failures.isEmpty(), "Items that fail to load: " + String.join(", ", failures));
    }
}
