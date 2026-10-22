package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.barn;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;

import java.util.ArrayList;
import java.util.List;

public abstract class MobBarnAnimal extends SkyBlockMob {
    protected MobBarnAnimal(EntityType type) {
        super(type);
    }

    protected abstract double health();

    @Override
    public Integer getLevel() {
        return 1;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(new VanillaRandomStrollGoal(this, 10));
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return new ArrayList<>();
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, health())
                .withBase(ItemStatistic.SPEED, 70D)
                .build();
    }

    @Override
    public SkillCategories getSkillCategory() {
        return SkillCategories.FARMING;
    }

    @Override
    public long damageCooldown() {
        return 200;
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(2, 0, 3);
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.ANIMAL);
    }
}
