package net.swofty.type.jerrysworkshop.defense;

import lombok.Getter;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.minestom.server.tag.Tag;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.gifts.SnowballProjectile;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A Magma Cube marching on the gift piles during Defend Jerry's Workshop. Only snowballs hurt it,
 * via a tag SnowballProjectile sets on its damage; everything else is ignored. Its movement is
 * driven by JerryWorkshopDefenseManager rather than goals, since its target is a pile, not a player.
 *
 * SkyBlockMob's constructor reads the name and health before fields are assigned, so the cube
 * travels through a thread-local during construction.
 */
public class WorkshopDefenseMob extends SkyBlockMob {
    public static final Tag<Boolean> SNOWBALL_DAMAGE_TAG = SnowballProjectile.SNOWBALL_DAMAGE_TAG;
    private static final ThreadLocal<DefendCube> CONSTRUCTING = new ThreadLocal<>();

    private final DefendCube cube;
    @Getter
    private final int wave;

    public WorkshopDefenseMob(DefendCube cube, int wave) {
        super(begin(cube));
        this.cube = cube;
        this.wave = wave;
        CONSTRUCTING.remove();
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(cube.getSize()));
    }

    private static EntityType begin(DefendCube cube) {
        CONSTRUCTING.set(cube);
        return EntityType.MAGMA_CUBE;
    }

    public DefendCube getCube() {
        return cube != null ? cube : CONSTRUCTING.get();
    }

    @Override
    public String getDisplayName() {
        return getCube().getDisplayName();
    }

    @Override
    public Integer getLevel() {
        return getCube().ordinal() + 1;
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
                .withBase(ItemStatistic.HEALTH, (double) getCube().getHealth())
                .withBase(ItemStatistic.SPEED, 80D)
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
        return 0;
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(0, 0, 0);
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.MAGMATIC, MobType.CUBIC);
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (!Boolean.TRUE.equals(damage.tagHandler().getTag(SNOWBALL_DAMAGE_TAG))) return false;

        boolean result = super.damage(damage);
        if (damage.getAttacker() instanceof SkyBlockPlayer player) {
            JerryWorkshopDefenseManager.recordDamage(player.getUuid(), damage.getAmount());
        }
        return result;
    }
}
