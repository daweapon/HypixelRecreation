package net.swofty.type.skyblockgeneric.entity.mob.mobs.hub;

import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.ai.RandomRegionStrollGoal;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Hub Sewer's Rat (wiki Rat (Hub)): a passive level 3 mob with 5,000 HP that drops 3 Tasty
 * Cheese, plus 2 more half the time. It has no Bestiary entry.
 */
public class MobSewerRat extends SkyBlockMob {
    private static final int GUARANTEED_CHEESE = 3;
    private static final int BONUS_CHEESE = 2;

    public MobSewerRat() {
        super(EntityType.SILVERFISH);
    }

    @Override
    public String getDisplayName() {
        return "Rat";
    }

    @Override
    public Integer getLevel() {
        return 3;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(new RandomRegionStrollGoal(this, 8, RegionType.SEWER));
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of();
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 5_000D)
                .withBase(ItemStatistic.SPEED, 28D)
                .build();
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        return new SkyBlockLootTable() {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                int cheese = GUARANTEED_CHEESE + (ThreadLocalRandom.current().nextBoolean() ? BONUS_CHEESE : 0);
                return List.of(new LootRecord(ItemType.TASTY_CHEESE, cheese, 100));
            }

            @Override
            public @NonNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
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
        return new OtherLoot(5, 1, 3);
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.ANIMAL);
    }
}
