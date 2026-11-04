package dev.daweapon.type.theend.dragon;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.damage.Damage;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.DoubleConsumer;

public final class DragonGateMob extends SkyBlockMob {
    private final DoubleConsumer onDamaged;

    public DragonGateMob(DoubleConsumer onDamaged) {
        super(EntityType.MAGMA_CUBE);
        this.onDamaged = onDamaged;
        setNoGravity(true);
        setInvisible(true);
        setCustomNameVisible(false);
    }

    public double damageTaken() {
        return getMaxHealth() - getHealth();
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (!(damage.getAttacker() instanceof SkyBlockPlayer) || isRemoved() || getHealth() <= 0) return false;
        setHealth(getHealth() - damage.getAmount());
        refreshName();
        onDamaged.accept(damageTaken());
        return true;
    }

    @Override public String getDisplayName() { return "Dragon's Gate"; }
    @Override public Integer getLevel() { return 1; }
    @Override public float getNameDisplayHeightOffset() { return 9f; }
    @Override public List<GoalSelector> getGoalSelectors() { return List.of(); }
    @Override public List<TargetSelector> getTargetSelectors() { return List.of(); }
    @Override public boolean hasCollision() { return false; }
    @Override public @Nullable SkyBlockLootTable getLootTable() { return null; }
    @Override public SkillCategories getSkillCategory() { return SkillCategories.COMBAT; }
    @Override public long damageCooldown() { return 0; }
    @Override public OtherLoot getOtherLoot() { return new OtherLoot(0, 0, 0); }
    @Override public List<MobType> getMobTypes() { return List.of(MobType.ENDER); }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder().withBase(ItemStatistic.HEALTH, DragonGate.HEALTH).build();
    }
}
