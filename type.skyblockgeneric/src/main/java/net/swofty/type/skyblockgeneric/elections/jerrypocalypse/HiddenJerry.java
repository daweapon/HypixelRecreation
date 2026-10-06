package net.swofty.type.skyblockgeneric.elections.jerrypocalypse;

import lombok.NonNull;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A Jerry revealed by Jerrypocalypse. It runs from the player who found it and, being Shielded,
 * loses exactly one health per melee or bow hit regardless of the weapon. Its box goes straight to
 * the player's inventory, as it has on Hypixel since 0.24.5.
 */
public class HiddenJerry extends SkyBlockMob {
    private static final ThreadLocal<HiddenJerryType> CONSTRUCTING = new ThreadLocal<>();

    private final HiddenJerryType type;
    private final SkyBlockPlayer finder;

    public HiddenJerry(HiddenJerryType type, SkyBlockPlayer finder) {
        super(begin(type));
        this.type = type;
        this.finder = finder;
        CONSTRUCTING.remove();
    }

    private static EntityType begin(HiddenJerryType type) {
        CONSTRUCTING.set(type);
        return EntityType.VILLAGER;
    }

    public HiddenJerryType getType() {
        return type != null ? type : CONSTRUCTING.get();
    }

    public void fleeFromFinder() {
        if (finder == null || finder.getInstance() != getInstance()) return;
        Vec away = getPosition().sub(finder.getPosition()).asVec().withY(0);
        if (away.lengthSquared() < 0.01) return;
        setVelocity(away.normalize().mul(6).withY(isOnGround() ? 3 : getVelocity().y()));
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (damage.getType() != DamageType.PLAYER_ATTACK) return false;
        return super.damage(new Damage(damage.getType(), damage.getSource(), damage.getAttacker(),
                damage.getSourcePosition(), 1f));
    }

    @Override
    public String getDisplayName() {
        return getType().getName() + " Jerry";
    }

    @Override
    public Integer getLevel() {
        return getType().getLevel();
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of();
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of();
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, (double) getType().getHealth())
                .withBase(ItemStatistic.DAMAGE, 1D)
                .withBase(ItemStatistic.SPEED, 120D)
                .build();
    }

    @Override
    public SkyBlockLootTable getLootTable() {
        HiddenJerryType jerry = getType();
        return new SkyBlockLootTable() {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                return List.of(new LootRecord(jerry.getBox(), 1, 100));
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
        return 200;
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(0, 0, 0);
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.HUMANOID, MobType.SHIELDED);
    }
}
