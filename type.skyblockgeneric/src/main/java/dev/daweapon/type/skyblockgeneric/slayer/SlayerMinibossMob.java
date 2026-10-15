package dev.daweapon.type.skyblockgeneric.slayer;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossBehaviour;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * A slayer miniboss spawned mid-quest. Its kill counts toward the quest like any target mob and
 * drops the slayer's token, with a bonus for veterans of the first three slayers.
 */
public final class SlayerMinibossMob extends SkyBlockMob {
    private static final int VETERAN_LEVEL = 6;
    private static final int VETERAN_BONUS = 3;
    private static final ThreadLocal<SlayerMiniboss> IN_CONSTRUCTION = new ThreadLocal<>();

    private final SlayerMiniboss miniboss;
    private final UUID ownerUuid;

    private SlayerMinibossMob(SlayerMiniboss miniboss, UUID ownerUuid) {
        super(EntityType.BLAZE);
        this.miniboss = miniboss;
        this.ownerUuid = ownerUuid;
    }

    public static SlayerMinibossMob create(SlayerMiniboss miniboss, UUID ownerUuid) {
        IN_CONSTRUCTION.set(miniboss);
        try {
            return new SlayerMinibossMob(miniboss, ownerUuid);
        } finally {
            IN_CONSTRUCTION.remove();
        }
    }

    public String minibossId() {
        return live().id();
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    @Override public String getDisplayName() { return live().name(); }
    @Override public Integer getLevel() { return live().level(); }
    @Override public List<MobType> getMobTypes() { return List.of(MobType.INFERNAL); }
    @Override public @Nullable SkyBlockLootTable getLootTable() { return null; }
    @Override public SkillCategories getSkillCategory() { return SkillCategories.COMBAT; }
    @Override public long damageCooldown() { return 500L; }
    @Override public OtherLoot getOtherLoot() { return new OtherLoot(live().combatXp(), 1, live().combatXp() * 2 / 5); }
    @Override public List<GoalSelector> getGoalSelectors() { return SlayerBossBehaviour.goals(this); }
    @Override public List<TargetSelector> getTargetSelectors() { return SlayerBossBehaviour.targets(this); }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
            .withBase(ItemStatistic.HEALTH, live().health())
            .withBase(ItemStatistic.DAMAGE, live().damage())
            .withBase(ItemStatistic.SPEED, 120D)
            .build();
    }

    @Override
    public void kill() {
        SkyBlockPlayer killer = getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player
            ? player : null;
        super.kill();
        if (killer == null || live().token() == null || live().tokenAmount() <= 0) return;
        int amount = live().tokenAmount() + (SlayerService.firstThreeBossesAtLevel(killer, VETERAN_LEVEL) ? VETERAN_BONUS : 0);
        CrimsonMobLoot.deliver(killer, this, new SkyBlockItem(live().token(), amount), false);
    }

    private SlayerMiniboss live() {
        return miniboss != null ? miniboss : IN_CONSTRUCTION.get();
    }
}
