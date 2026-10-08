package net.swofty.type.skyblockgeneric.slayer;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlayerRngMeterFillTest {

    @BeforeAll
    static void load() {
        SlayerRegistry.loadAll(new File("../configuration/skyblock/slayers.yml"));
    }

    @Test
    void fillsEveryEnabledSlayersMeterToFull() {
        for (SlayerDefinition definition : SlayerRegistry.all()) {
            if (definition.drops().stream().noneMatch(drop -> drop.hasRngMeter() && drop.available())) continue;
            DatapointSlayer.SlayerProgress progress = new DatapointSlayer.SlayerProgress();
            SlayerRngMeter.fill(definition, progress);

            SlayerDrop selected = SlayerRngMeter.selected(definition, progress);
            assertNotNull(selected, definition.type() + " selected nothing");
            assertTrue(SlayerRngMeter.full(progress, selected), definition.type() + " meter not full");
        }
    }

    @Test
    void keepsThePlayersChoiceAndIsIdempotent() {
        SlayerDefinition blaze = SlayerRegistry.get(SlayerType.INFERNO_DEMONLORD);
        SlayerDrop cheapest = blaze.drops().stream()
            .filter(drop -> drop.hasRngMeter() && drop.available())
            .min(java.util.Comparator.comparingLong(SlayerDrop::rngMeterXp)).orElseThrow();
        DatapointSlayer.SlayerProgress progress = new DatapointSlayer.SlayerProgress();
        progress.setRngItem(cheapest.key());

        SlayerRngMeter.fill(blaze, progress);
        SlayerRngMeter.fill(blaze, progress);

        assertEquals(cheapest.key(), progress.getRngItem());
        assertEquals(cheapest.rngMeterXp(), progress.getRngXp());
    }
}
