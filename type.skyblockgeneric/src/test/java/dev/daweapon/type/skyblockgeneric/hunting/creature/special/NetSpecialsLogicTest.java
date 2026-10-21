package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetSpecialsLogicTest {
    @Test
    void standStillGoesRedYellowReady() {
        StandStillTimer timer = new StandStillTimer(0, 5, 8);
        assertEquals(StandStillTimer.Phase.IDLE, timer.tick(false, false));
        assertEquals(StandStillTimer.Phase.RED, timer.tick(true, true));
        for (int i = 0; i < 4; i++) assertEquals(StandStillTimer.Phase.RED, timer.tick(true, false));
        assertEquals(StandStillTimer.Phase.YELLOW, timer.tick(true, false));
        for (int i = 0; i < 2; i++) assertEquals(StandStillTimer.Phase.YELLOW, timer.tick(true, false));
        assertEquals(StandStillTimer.Phase.READY, timer.tick(true, false));
        assertEquals(StandStillTimer.Phase.RED, timer.tick(true, true));
    }

    @Test
    void standStillDelayPostponesCountdown() {
        StandStillTimer timer = new StandStillTimer(2, 3, 5);
        timer.tick(true, true);
        for (int i = 0; i < 4; i++) assertEquals(StandStillTimer.Phase.RED, timer.tick(true, false));
        assertEquals(StandStillTimer.Phase.YELLOW, timer.tick(true, false));
        for (int i = 0; i < 3; i++) timer.tick(true, false);
        assertEquals(StandStillTimer.Phase.READY, timer.tick(true, false));
    }

    @Test
    void spinMeterFaintsBetweenNinetyAndHundred() {
        SpinMeter meter = new SpinMeter();
        assertEquals(SpinMeter.Result.STARTED, meter.add(40));
        assertEquals(SpinMeter.Result.SPINNING, meter.add(40));
        assertEquals(SpinMeter.Result.KNOCKED_OUT, meter.add(15));
    }

    @Test
    void spinMeterExplodesOverHundred() {
        SpinMeter meter = new SpinMeter();
        meter.add(60);
        assertEquals(SpinMeter.Result.EXPLODED, meter.add(45));
    }

    @Test
    void spinMeterDecaysToStop() {
        SpinMeter meter = new SpinMeter();
        meter.add(10);
        meter.decay(20);
        assertEquals(false, meter.spinning());
        assertEquals(SpinMeter.Result.STARTED, meter.add(10));
    }

    @Test
    void pullDecidesSymbols() {
        assertEquals(1, PullSymbols.gained(5));
        assertEquals(1, PullSymbols.gained(25));
        assertEquals(2, PullSymbols.gained(50));
        assertEquals(4, PullSymbols.gained(500));
        assertEquals(4, PullSymbols.total(3, 5));
    }

    @Test
    void caveHideRunsThroughPhases() {
        CaveHide hide = new CaveHide(100, 50);
        assertEquals(CaveHide.Phase.OPEN, hide.phase());
        hide.startHiding(0);
        assertEquals(CaveHide.Phase.HIDING, hide.tick(10, false));
        assertEquals(CaveHide.Phase.IN_CAVE, hide.tick(20, true));
        assertEquals(CaveHide.Phase.IN_CAVE, hide.tick(60, false));
        assertEquals(CaveHide.Phase.OPEN, hide.tick(70, false));
    }

    @Test
    void caveHideGivesUpTravelling() {
        CaveHide hide = new CaveHide(100, 50);
        hide.startHiding(0);
        assertEquals(CaveHide.Phase.IN_CAVE, hide.tick(100, false));
    }

    @Test
    void shellwiseShardsFollowMaxHealth() {
        List<ShardsByMaxHpSpecial.Variant> variants = List.of(
                new ShardsByMaxHpSpecial.Variant(1000, 1), new ShardsByMaxHpSpecial.Variant(1500, 2), new ShardsByMaxHpSpecial.Variant(2000, 3));
        assertEquals(1, ShardsByMaxHpSpecial.shardsFor(variants, 1000, 1));
        assertEquals(2, ShardsByMaxHpSpecial.shardsFor(variants, 1500, 1));
        assertEquals(3, ShardsByMaxHpSpecial.shardsFor(variants, 2000, 1));
        assertEquals(1, ShardsByMaxHpSpecial.shardsFor(List.of(), 1500, 1));
    }
}
