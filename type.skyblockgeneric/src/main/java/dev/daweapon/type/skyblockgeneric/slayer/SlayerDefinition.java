package dev.daweapon.type.skyblockgeneric.slayer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;

public record SlayerDefinition(
    SlayerType type,
    boolean enabled,
    Optional<SlayerUnlockRequirement> unlockRequirement,
    List<MobType> targetMobTypes,
    Set<String> targetMobIds,
    List<MobType> bossMobTypes,
    List<SlayerLevelReward> levels,
    Map<SlayerTier, SlayerTierDefinition> tiers,
    List<SlayerDrop> drops,
    List<SlayerMiniboss> minibosses
) {
    public boolean accepts(SkyBlockMob mob) {
        if (mob instanceof SlayerMinibossMob miniboss) {
            return targetMobIds.contains(miniboss.minibossId());
        }
        if (!targetMobIds.isEmpty()) {
            return mob instanceof BestiaryMob bestiaryMob && targetMobIds.contains(bestiaryMob.getMobID());
        }
        return accepts(mob.getMobTypes());
    }

    public boolean accepts(List<MobType> mobTypes) {
        return mobTypes.stream().anyMatch(targetMobTypes::contains);
    }

    public int levelForXp(int xp) {
        int level = 0;
        for (SlayerLevelReward reward : levels) {
            if (xp >= reward.requiredXp()) {
                level = reward.level();
            }
        }
        return level;
    }

    public int maxXp() {
        return levels.stream().mapToInt(SlayerLevelReward::requiredXp).max().orElse(0);
    }

    public boolean hasDropTable() {
        return !drops.isEmpty();
    }
}
