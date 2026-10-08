package net.swofty.type.skyblockgeneric.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.ai.Intimidation;
import net.swofty.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import net.swofty.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A mob a combat minion spawns and then slays. The minion rolls its own drop table, so the mob
 * carries no loot of its own. Subclasses return constants only: SkyBlockMob's constructor calls
 * these getters before subclass fields exist.
 */
public abstract class MinionSpawnedMob extends SkyBlockMob {
    protected MinionSpawnedMob(EntityType entityType) {
        super(entityType);
    }

    protected abstract double health();

    protected boolean meleeAttacker() {
        return true;
    }

    @Override
    public Integer getLevel() {
        return 1;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        if (!meleeAttacker()) return List.of(new VanillaRandomStrollGoal(this, 15));
        return List.of(
                new VanillaMeleeAttackGoal(this, 1.6, 20, TimeUnit.SERVER_TICK),
                new VanillaRandomStrollGoal(this, 15)
        );
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        if (!meleeAttacker()) return List.of();
        return List.of(
                new LastEntityDamagerTarget(this, 16),
                new ClosestEntityTarget(this, 16, entity -> entity instanceof SkyBlockPlayer && !Intimidation.deters(this, entity))
        );
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, health())
                .withBase(ItemStatistic.SPEED, 70D)
                .build();
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        return null;
    }

    @Override
    public SkillCategories getSkillCategory() {
        return SkillCategories.COMBAT;
    }

    @Override
    public long damageCooldown() {
        return 200;
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(0, 0, 5);
    }
}
