package net.swofty.type.skyblockgeneric.item;

import net.swofty.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemIdsDefinedOnceTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

    @Test
    void everyItemIdIsDefinedInExactlyOnePlace() throws IOException {
        Map<String, Path> firstSeen = new HashMap<>();
        List<String> duplicates = new ArrayList<>();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).sorted().toList()) {
                if (!(YamlFileUtils.loadYaml(file.toFile()).get("items") instanceof List<?> items)) continue;
                for (Object entry : items) {
                    if (!(entry instanceof Map<?, ?> item) || !(item.get("id") instanceof String id)) continue;
                    Path earlier = firstSeen.putIfAbsent(id, file);
                    if (earlier != null) duplicates.add(id + " in " + ITEMS.relativize(earlier) + " and " + ITEMS.relativize(file));
                }
            }
        }
        assertTrue(duplicates.isEmpty(), "the loader keeps whichever copy it reads last: " + duplicates);
    }
}
