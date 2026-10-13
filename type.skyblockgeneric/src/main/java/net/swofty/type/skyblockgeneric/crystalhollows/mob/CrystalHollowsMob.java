package net.swofty.type.skyblockgeneric.crystalhollows.mob;

import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.ai.ClosestEntityRegionTarget;
import net.swofty.type.skyblockgeneric.entity.mob.ai.MeleeAttackWithinRegionGoal;
import net.swofty.type.skyblockgeneric.entity.mob.ai.RandomRegionStrollGoal;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class CrystalHollowsMob extends BestiaryMob {
    protected CrystalHollowsMob(EntityType entityType) {
        super(entityType);
    }

    protected abstract CrystalHollowsMobSpec spec();

    protected abstract RegionType homeRegion();

    @Override
    public String getDisplayName() {
        return spec().name();
    }

    @Override
    public Integer getLevel() {
        return spec().level();
    }

    @Override
    public String getMobID() {
        return spec().id();
    }

    @Override
    public int getMaxBestiaryTier() {
        return spec().maxTier();
    }

    @Override
    public int getBestiaryBracket() {
        return spec().bracket();
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return spec().icon();
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        CrystalHollowsMobSpec spec = spec();
        RegionType region = homeRegion();
        if (!spec.aggressive()) {
            return List.of(new RandomRegionStrollGoal(this, spec.strollRadius(), region));
        }
        return List.of(
                new MeleeAttackWithinRegionGoal(this, 1.6, 20, TimeUnit.SERVER_TICK, region),
                new RandomRegionStrollGoal(this, spec.strollRadius(), region)
        );
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        CrystalHollowsMobSpec spec = spec();
        if (!spec.aggressive() || spec.aggroRange() <= 0) {
            return List.of(new LastEntityDamagerTarget(this, 16));
        }
        return List.of(
                new LastEntityDamagerTarget(this, 16),
                new ClosestEntityRegionTarget(this, spec.aggroRange(), entity -> entity instanceof SkyBlockPlayer, homeRegion())
        );
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        CrystalHollowsMobSpec spec = spec();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, spec.health())
                .withBase(ItemStatistic.DAMAGE, spec.damage())
                .withBase(ItemStatistic.SPEED, spec.speed())
                .build();
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        List<CrystalHollowsMobSpec.Drop> drops = spec().drops();
        if (drops.isEmpty()) return null;
        List<SkyBlockLootTable.LootRecord> records = drops.stream()
                .map(drop -> new SkyBlockLootTable.LootRecord(drop.item(), drop.min(), drop.chance()))
                .toList();
        return new SkyBlockLootTable() {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                return records;
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }

    @Override
    public SkyBlockItem createDrop(ItemType type, int amount) {
        for (CrystalHollowsMobSpec.Drop drop : spec().drops()) {
            if (drop.item().equals(type) && drop.max() > drop.min()) {
                amount += ThreadLocalRandom.current().nextInt(drop.max() - drop.min() + 1);
                break;
            }
        }
        return super.createDrop(type, amount);
    }

    @Override
    public SkillCategories getSkillCategory() {
        return SkillCategories.COMBAT;
    }

    @Override
    public long damageCooldown() {
        return spec().damageCooldown();
    }

    @Override
    public OtherLoot getOtherLoot() {
        CrystalHollowsMobSpec spec = spec();
        return new OtherLoot(spec.combatXp(), spec.coins(), spec.orbs());
    }

    @Override
    public List<MobType> getMobTypes() {
        return spec().types();
    }
}
