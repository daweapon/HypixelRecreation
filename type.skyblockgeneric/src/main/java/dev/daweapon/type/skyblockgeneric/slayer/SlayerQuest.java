package dev.daweapon.type.skyblockgeneric.slayer;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public record SlayerQuest(
    SlayerType type,
    SlayerTier tier,
    long startedAt,
    int combatXp,
    boolean bossSpawned,
    @Nullable UUID bossUuid,
    int lastMinibossXp,
    long lastMinibossAt
) {
    public SlayerQuest(SlayerType type, SlayerTier tier, long startedAt, int combatXp, boolean bossSpawned, @Nullable UUID bossUuid) {
        this(type, tier, startedAt, combatXp, bossSpawned, bossUuid, 0, 0L);
    }

    public SlayerQuest addCombatXp(int amount, int requiredCombatXp) {
        return new SlayerQuest(type, tier, startedAt, Math.min(requiredCombatXp, combatXp + amount), false, null,
            lastMinibossXp, lastMinibossAt);
    }

    public SlayerQuest markBossSpawned(UUID bossUuid) {
        return new SlayerQuest(type, tier, startedAt, combatXp, true, bossUuid, lastMinibossXp, lastMinibossAt);
    }

    public SlayerQuest markMinibossSpawned(long now) {
        return new SlayerQuest(type, tier, startedAt, combatXp, bossSpawned, bossUuid, combatXp, now);
    }
}
