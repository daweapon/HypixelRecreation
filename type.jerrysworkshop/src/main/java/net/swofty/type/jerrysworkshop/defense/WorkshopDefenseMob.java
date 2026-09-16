package net.swofty.type.jerrysworkshop.defense;

import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import net.swofty.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// A wave mob for the "Defend Jerry's Workshop" event (Roadmap #20). Real Hypixel: these mobs "can
// only be damaged with snowballs" - implemented here via a tag on the Damage object (see
// SnowballEntityImpl), set only for snowball-collision damage; damage() rejects anything else
// outright (melee, arrows, magic all do nothing to these mobs, matching the real restriction).
public class WorkshopDefenseMob extends SkyBlockMob {

    public static final Tag<Boolean> SNOWBALL_DAMAGE_TAG = Tag.Boolean("workshop_snowball_damage");

    private final WorkshopMobVariant variant;
    private final int roundNumber;

    public WorkshopDefenseMob(WorkshopMobVariant variant, int roundNumber) {
        super(variant.getEntityType());
        this.variant = variant;
        this.roundNumber = roundNumber;
    }

    @Override
    public String getDisplayName() {
        return variant.getDisplayName();
    }

    @Override
    public Integer getLevel() {
        return roundNumber;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(
                new VanillaMeleeAttackGoal(this, 1.4, 16, TimeUnit.SERVER_TICK),
                new VanillaRandomStrollGoal(this, 10)
        );
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of(
                new LastEntityDamagerTarget(this, 20),
                new ClosestEntityTarget(this, 20, entity -> entity instanceof SkyBlockPlayer)
        );
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 150D + (100D * roundNumber))
                .withBase(ItemStatistic.DAMAGE, 10D + (5D * roundNumber))
                .withBase(ItemStatistic.SPEED, 90D)
                .build();
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        // Rewards for this event come from JerryWorkshopDefenseManager's round-end damage
        // distribution, not per-kill loot.
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
        return List.of(MobType.FROZEN);
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
