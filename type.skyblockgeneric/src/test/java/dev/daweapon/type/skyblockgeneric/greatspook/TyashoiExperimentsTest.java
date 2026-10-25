package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TyashoiExperimentsTest {
    @Test
    void catalogueHasEveryTyashoiTrade() {
        assertEquals(22, TyashoiExperiments.all().size());
    }

    @Test
    void requirementsPointAtEarlierTrades() {
        for (TyashoiExperiment experiment : TyashoiExperiments.all().values()) {
            if (experiment.requires() == null) continue;
            assertNotNull(TyashoiExperiments.get(experiment.requires()), experiment.id());
        }
    }

    @Test
    void everyResultItemExists() {
        for (TyashoiExperiment experiment : TyashoiExperiments.all().values()) {
            if (experiment.kind() == TyashoiExperiment.Kind.BOOK) continue;
            assertNotNull(ItemType.get(experiment.resultItem()), experiment.id());
        }
    }

    @Test
    void everyIngredientIsACatalogueItem() {
        Set<String> unresolved = new TreeSet<>();
        for (TyashoiExperiment experiment : TyashoiExperiments.all().values()) {
            for (TyashoiExperiment.Cost cost : experiment.costs()) {
                if (!cost.isCoins() && cost.type() == null) unresolved.add(cost.item());
            }
        }
        assertEquals(new TreeSet<String>(), unresolved);
    }

    @Test
    void everyTradeHasAHintAndLockoutsAreWithinAWeek() {
        for (TyashoiExperiment experiment : TyashoiExperiments.all().values()) {
            assertFalse(experiment.hint().isEmpty(), experiment.id());
            assertTrue(experiment.lockoutSeconds() < 7 * 86400, experiment.id());
        }
    }

    @Test
    void formatsLockoutsLikeTheWiki() {
        assertEquals("2d 23h 59m 59s", TyashoiService.formatDuration(259199_000L));
    }
}
