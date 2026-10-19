package dev.daweapon.type.theend.dragon;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class DragonCrystalMob extends SkyBlockMob {
    private final Consumer<SkyBlockPlayer> onDestroyed;

    public DragonCrystalMob(Consumer<SkyBlockPlayer> onDestroyed) {
        super(EntityType.END_CRYSTAL);
        this.onDestroyed = onDestroyed;
        setNoGravity(true);
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (!(damage.getAttacker() instanceof SkyBlockPlayer player) || isRemoved()) return false;
        onDestroyed.accept(player);
        remove();
        return true;
    }

    @Override public String getDisplayName() { return "Ender Crystal"; }
    @Override public Integer getLevel() { return 1; }
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
        return ItemStatistics.builder().withBase(ItemStatistic.HEALTH, 1D).build();
    }
}
