package net.swofty.type.skyblockgeneric.tabmodules;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountInformationModuleTest {
    @Test
    void statsAreRoundedAndCommaSeparated() {
        ItemStatistics statistics = ItemStatistics.builder()
                .withBase(ItemStatistic.SPEED, 147.0439956672135)
                .withBase(ItemStatistic.STRENGTH, 1828.4399566721348)
                .build();
        assertTrue(plain(ItemStatistic.SPEED, statistics).endsWith("147"), plain(ItemStatistic.SPEED, statistics));
        assertTrue(plain(ItemStatistic.STRENGTH, statistics).endsWith("1,828"), plain(ItemStatistic.STRENGTH, statistics));
    }

    @Test
    void everyStatLineHasOnePlaceholderForItsValue() throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of("../configuration/i18n/en_US/tablist.properties"))) {
            properties.load(reader);
        }
        for (String stat : List.of("speed", "strength", "crit_chance", "crit_damage", "attack_speed")) {
            String line = properties.getProperty("tablist.account_info." + stat);
            assertTrue(line.endsWith("{0}"), stat + ": " + line);
            assertFalse(line.contains("{1}"), stat + ": " + line);
        }
        assertEquals("<aqua><bold>Area: <gray>Kuudra", properties.getProperty("tablist.server_info.area.kuudra"));
    }

    private static String plain(ItemStatistic statistic, ItemStatistics statistics) {
        return PlainTextComponentSerializer.plainText().serialize(AccountInformationModule.stat(statistic, statistics));
    }
}
