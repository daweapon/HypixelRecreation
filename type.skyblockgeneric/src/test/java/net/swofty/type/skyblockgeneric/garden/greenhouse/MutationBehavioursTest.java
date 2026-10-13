package net.swofty.type.skyblockgeneric.garden.greenhouse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MutationBehavioursTest {
    @Test
    void chloroniteDropsFollowTheMiningFortuneBrackets() {
        assertEquals(0, MutationBehaviours.chloroniteDrops(0), 1e-9);
        assertEquals(0.5, MutationBehaviours.chloroniteDrops(50), 1e-9);
        assertEquals(1, MutationBehaviours.chloroniteDrops(100), 1e-9);
        assertEquals(1.5, MutationBehaviours.chloroniteDrops(300), 1e-9);
        assertEquals(2, MutationBehaviours.chloroniteDrops(500), 1e-9);
        assertEquals(3, MutationBehaviours.chloroniteDrops(1000), 1e-9);
        assertEquals(3.5, MutationBehaviours.chloroniteDrops(1500), 1e-9);
        assertEquals(4, MutationBehaviours.chloroniteDrops(2000), 1e-9);
        assertEquals(4, MutationBehaviours.chloroniteDrops(9999), 1e-9);
    }

    @Test
    void aloeFragmentsAndResetChanceFollowTheWikiTable() {
        assertEquals(0, MutationBehaviours.aloeFragments(3));
        assertEquals(1, MutationBehaviours.aloeFragments(4));
        assertEquals(13, MutationBehaviours.aloeFragments(10));
        assertEquals(7462, MutationBehaviours.aloeFragments(27));
        assertEquals(0, MutationBehaviours.aloeResetChance(3), 1e-9);
        assertEquals(3, MutationBehaviours.aloeResetChance(4), 1e-9);
        assertEquals(72, MutationBehaviours.aloeResetChance(27), 1e-9);
    }

    @Test
    void jellybeanMultiplierGrowsEveryTwelveStagesToTenTimes() {
        assertEquals(0, MutationBehaviours.jellybeanMultiplier(11));
        assertEquals(1, MutationBehaviours.jellybeanMultiplier(12));
        assertEquals(1, MutationBehaviours.jellybeanMultiplier(23));
        assertEquals(2, MutationBehaviours.jellybeanMultiplier(24));
        assertEquals(10, MutationBehaviours.jellybeanMultiplier(120));
    }

    @Test
    void mealsRestoreHungerByTier() {
        assertEquals(2, MutationBehaviours.mealHunger("RAW_BEEF"));
        assertEquals(4, MutationBehaviours.mealHunger("ENCHANTED_PORK"));
        assertEquals(6, MutationBehaviours.mealHunger("ENCHANTED_COOKED_MUTTON"));
        assertEquals(0, MutationBehaviours.mealHunger("ENCHANTED_RABBIT_FOOT"));
        assertEquals(0, MutationBehaviours.mealHunger("WHEAT"));
        assertEquals(40, MutationBehaviours.mealBonus("ENCHANTED_COOKED_MUTTON"), 1e-9);
        assertEquals(20, MutationBehaviours.mealBonus("ENCHANTED_PORK"), 1e-9);
    }

    @Test
    void waterlessMutationsDoNotNeedWater() {
        for (String id : new String[]{"CINDERSHADE", "ZOMBUD", "PHANTOMLEAF", "THUNDERLING", "ALL_IN_ALOE", "PLANTBOY_ADVANCE", "JERRYFLOWER", "CHORUS_FRUIT"}) {
            assertFalse(MutationBehaviours.waterable(id), id);
        }
        for (String id : new String[]{"SNOOZLING", "BLASTBERRY", "GLASSCORN", "WHEAT"}) {
            assertTrue(MutationBehaviours.waterable(id), id);
        }
        assertFalse(MutationBehaviours.waterable("SOGGYBUD"));
    }

    @Test
    void neverDecayingMutationsAreFlagged() {
        MutationCatalogue catalogue = MutationCatalogue.get();
        assertTrue(catalogue.mutation("ALL_IN_ALOE").neverDecays());
        assertTrue(catalogue.mutation("FLESHTRAP").neverDecays());
        assertTrue(catalogue.mutation("MAGIC_JELLYBEAN").neverDecays());
        assertFalse(catalogue.mutation("DUSTGRAIN").neverDecays());
    }

    @Test
    void stoplightCyclesGreenYellowRed() {
        assertEquals(GreenhouseMinigames.Light.GREEN, GreenhouseMinigames.light(0));
        assertEquals(GreenhouseMinigames.Light.YELLOW, GreenhouseMinigames.light(GreenhouseMinigames.LIGHT_GREEN_MILLIS));
        assertEquals(GreenhouseMinigames.Light.RED, GreenhouseMinigames.light(GreenhouseMinigames.LIGHT_GREEN_MILLIS + GreenhouseMinigames.LIGHT_YELLOW_MILLIS));
        assertEquals(GreenhouseMinigames.Light.GREEN, GreenhouseMinigames.light(GreenhouseMinigames.LIGHT_GREEN_MILLIS + GreenhouseMinigames.LIGHT_YELLOW_MILLIS + GreenhouseMinigames.LIGHT_RED_MILLIS));
    }

    @Test
    void snakeLosesOnTheWall() {
        SnakeGame game = new SnakeGame(new java.util.Random(1));
        for (int i = 0; i < SnakeGame.COLUMNS && game.state() == SnakeGame.State.PLAYING; i++) game.move(SnakeGame.Direction.LEFT);
        assertEquals(SnakeGame.State.LOST, game.state());
    }

    @Test
    void snakeGrowsByOneTilePerApple() {
        SnakeGame game = new SnakeGame(new java.util.Random(5));
        int steps = 0;
        while (game.apples() == 0 && steps++ < 50) {
            int head = game.head();
            int apple = game.apple();
            int hc = head % SnakeGame.COLUMNS;
            int hr = head / SnakeGame.COLUMNS;
            int ac = apple % SnakeGame.COLUMNS;
            int ar = apple / SnakeGame.COLUMNS;
            if (hc != ac) game.move(hc < ac ? SnakeGame.Direction.RIGHT : SnakeGame.Direction.LEFT);
            else game.move(hr < ar ? SnakeGame.Direction.DOWN : SnakeGame.Direction.UP);
        }
        assertEquals(1, game.apples());
        assertEquals(2, game.length());
        assertEquals(SnakeGame.State.PLAYING, game.state());
    }
}
