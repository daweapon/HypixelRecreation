package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import lombok.NonNull;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.ai.ClosestEntityRegionTarget;
import net.swofty.type.skyblockgeneric.entity.mob.ai.MeleeAttackWithinRegionGoal;
import net.swofty.type.skyblockgeneric.entity.mob.ai.RandomRegionStrollGoal;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.goblin.MobGoblin;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobHoldoutGoblin extends MobGoblin {
    @Override
    public Integer getLevel() {
        return 3;
    }

    @Override
    public String getMobID() {
        return "GOBLIN_HOLDOUT_WEAKLING";
    }

    @Override
    public String getDisplayName() {
        return "Weakling";
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(
                new MeleeAttackWithinRegionGoal(this, 1.6, 20, TimeUnit.SERVER_TICK, RegionType.GOBLIN_HOLDOUT, false),
                new RandomRegionStrollGoal(this, 8, RegionType.GOBLIN_HOLDOUT)
        );
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of(
                new LastEntityDamagerTarget(this, 16),
                new ClosestEntityRegionTarget(this, 8, entity -> entity instanceof SkyBlockPlayer, RegionType.GOBLIN_HOLDOUT)
        );
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 200D)
                .withBase(ItemStatistic.DAMAGE, 50D)
                .withBase(ItemStatistic.SPEED, 100D)
                .build();
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        return new SkyBlockLootTable() {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                return List.of(new LootRecord(ItemType.GOBLIN_EGG, 1, 1));
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(60, 10, 30);
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.GOBLIN_HOLDOUT, 4));
    }
}
