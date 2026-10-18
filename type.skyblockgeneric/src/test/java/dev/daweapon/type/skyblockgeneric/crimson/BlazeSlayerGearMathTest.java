package dev.daweapon.type.skyblockgeneric.crimson;

import dev.daweapon.type.skyblockgeneric.item.set.sets.ShimmeringLightSet;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazeDagger;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazeSlayerGear;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazetekkHamRadio;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazetekkHamRadio.Signal;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.TacticalInsertion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlazeSlayerGearMathTest {
    @Test
    void shimmerBonusFollowsPieceCount() {
        assertEquals(1, ShimmeringLightSet.experienceOrbMultiplier(1), 1e-9);
        assertEquals(3, ShimmeringLightSet.experienceOrbMultiplier(2), 1e-9);
        assertEquals(3, ShimmeringLightSet.experienceOrbMultiplier(3), 1e-9);
        assertEquals(4, ShimmeringLightSet.experienceOrbMultiplier(4), 1e-9);
    }

    @Test
    void daggerCombatWisdomOnlyAgainstInfernalMobs() {
        assertEquals(30, BlazeSlayerGear.daggerCombatXp(BlazeDagger.MAWDREDGE, true, 100), 1e-9);
        assertEquals(0, BlazeSlayerGear.daggerCombatXp(BlazeDagger.MAWDREDGE, false, 100), 1e-9);
        assertEquals(45, BlazeSlayerGear.daggerCombatXp(BlazeDagger.DEATHRIPPER, true, 100), 1e-9);
    }

    @Test
    void hamRadioSignalDependsOnOthersOnChannel() {
        assertEquals(Signal.WEAK, BlazetekkHamRadio.signal(0));
        assertEquals(Signal.STRONG, BlazetekkHamRadio.signal(1));
        assertEquals(Signal.LOST, BlazetekkHamRadio.signal(2));
        assertEquals(0.5, BlazetekkHamRadio.statMultiplier(Signal.WEAK), 1e-9);
        assertEquals(0, BlazetekkHamRadio.statMultiplier(Signal.LOST), 1e-9);
    }

    @Test
    void hamRadioChannelsWrapAfterNine() {
        assertEquals(9, BlazetekkHamRadio.CHANNELS.size());
        assertEquals(1, BlazetekkHamRadio.nextChannel(0));
        assertEquals(0, BlazetekkHamRadio.nextChannel(8));
        assertEquals("610 AM", BlazetekkHamRadio.channelName(0));
    }

    @Test
    void tacticalInsertionBurnsTenPercentOverSixSeconds() {
        assertEquals(10, TacticalInsertion.burnPerSecond(600), 1e-9);
        assertEquals(50, TacticalInsertion.halfHealth(100), 1e-9);
        assertEquals(1, TacticalInsertion.halfHealth(1), 1e-9);
    }
}
