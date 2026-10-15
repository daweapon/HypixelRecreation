package dev.daweapon.type.skyblockgeneric.entity.mob;

import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.levels.causes.CollectedMobTypesCause;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectedMobTypesTest {
    @Test
    void everyTypePlusTheCompletionBonusReachesTheCap() {
        double total = Arrays.stream(MobType.values())
                .mapToDouble(type -> new CollectedMobTypesCause(type.name()).xpReward())
                .sum()
                + new CollectedMobTypesCause(CollectedMobTypesCause.ALL_TYPES).xpReward();

        assertEquals(SkyBlockXPSource.COLLECT_MOB_TYPES.getMaxXP(), total);
    }
}
