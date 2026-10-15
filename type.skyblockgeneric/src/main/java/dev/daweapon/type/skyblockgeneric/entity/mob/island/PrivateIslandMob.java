package dev.daweapon.type.skyblockgeneric.entity.mob.island;

import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.utils.time.TimeUnit;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.Intimidation;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A naturally spawning Private Island monster of one species at one level.
 *
 * SkyBlockMob's constructor reads the mob's stats before a subclass can assign fields, so the
 * species and level travel through {@link #begin} into a thread-local for the duration of
 * construction.
 */
public class PrivateIslandMob extends BestiaryMob {
    private static final ThreadLocal<Spec> CONSTRUCTING = new ThreadLocal<>();

    private record Spec(IslandMobSpecies species, int level) {
    }

    private final Spec spec;

    protected PrivateIslandMob(IslandMobSpecies species, int level) {
        super(begin(new Spec(species, species.clamp(level))));
        this.spec = CONSTRUCTING.get();
        CONSTRUCTING.remove();
    }

    public static PrivateIslandMob of(IslandMobSpecies species, int level) {
        return new PrivateIslandMob(species, level);
    }

    private static EntityType begin(Spec spec) {
        CONSTRUCTING.set(spec);
        return spec.species().getEntityType();
    }

    private Spec spec() {
        return spec != null ? spec : CONSTRUCTING.get();
    }

    public IslandMobSpecies getSpecies() {
        return spec().species();
    }

    @Override
    public String getDisplayName() {
        return spec().species().getDisplayName();
    }

    @Override
    public Integer getLevel() {
        return spec().level();
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(
                new VanillaMeleeAttackGoal(this, 1.6, 20, TimeUnit.SERVER_TICK),
                new VanillaRandomStrollGoal(this, 10));
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        if (spec().species() == IslandMobSpecies.ENDERMAN) {
            return List.of(new LastEntityDamagerTarget(this, 16));
        }
        return List.of(
                new LastEntityDamagerTarget(this, 16),
                new ClosestEntityTarget(this, 16,
                        entity -> entity instanceof SkyBlockPlayer && !Intimidation.deters(this, entity)));
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        IslandMobSpecies species = spec().species();
        int level = spec().level();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, (double) species.healthAt(level))
                .withBase(ItemStatistic.DAMAGE, (double) species.damageAt(level))
                .withBase(ItemStatistic.SPEED, 100D)
                .build();
    }

    @Override
    public SkyBlockLootTable getLootTable() {
        IslandMobSpecies species = spec().species();
        return new SkyBlockLootTable() {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                return species.getDrops().get();
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
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
        return 500;
    }

    // Private Island kills have given no Combat XP since 2020 outside Stranded profiles.
    @Override
    public OtherLoot getOtherLoot() {
        int level = spec().level();
        return new OtherLoot(0, spec().species().coinsAt(level), spec().species().orbsAt(level));
    }

    @Override
    public List<MobType> getMobTypes() {
        return spec().species().getMobTypes();
    }

    @Override
    public int getMaxBestiaryTier() {
        return 5;
    }

    @Override
    public int getBestiaryBracket() {
        return 2;
    }

    @Override
    public String getMobID() {
        return String.format("%s_%02d", spec().species().name(), spec().level());
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return new GUIMaterial(spec().species().getIcon());
    }
}
