package net.swofty.type.skyblockgeneric.pets;

import net.swofty.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PetPerkCoverageTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

    @Test
    void everyPetPerkIsActiveOrSaysWhyItIsUnavailable() throws IOException {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                if (!(YamlFileUtils.loadYaml(file.toFile()).get("items") instanceof List<?> items)) continue;
                for (Object item : items) {
                    if (!(item instanceof Map<?, ?> itemMap) || !(itemMap.get("components") instanceof List<?> components)) continue;
                    for (Object component : components) {
                        if (!(component instanceof Map<?, ?> map) || !"PET".equals(map.get("id"))) continue;
                        if (!(map.get("abilities") instanceof List<?> abilities)) continue;
                        for (Object ability : abilities) {
                            if (!(ability instanceof Map<?, ?> perk)) continue;
                            String kind = String.valueOf(perk.get("kind") == null ? "INACTIVE" : perk.get("kind"));
                            if (kind.equalsIgnoreCase("INACTIVE") && perk.get("unavailable") == null) {
                                offenders.add(itemMap.get("id") + " / " + perk.get("name"));
                            }
                        }
                    }
                }
            }
        }
        assertTrue(offenders.isEmpty(), "Perks with no effect and no unavailable reason: " + offenders);
    }
}
