package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer;

import java.util.List;
import java.util.UUID;
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
import org.jetbrains.annotations.Nullable;

public final class SlayerMinionMob extends SkyBlockMob {
    private static final ThreadLocal<SlayerMinionProfile> IN_CONSTRUCTION = new ThreadLocal<>();

    private final SlayerMinionProfile profile;
    private final UUID ownerUuid;

    private SlayerMinionMob(UUID ownerUuid, SlayerMinionProfile profile) {
        super(profile.entityType());
        this.ownerUuid = ownerUuid;
        this.profile = profile;
    }

    public static SlayerMinionMob create(UUID ownerUuid, SlayerMinionProfile profile) {
        IN_CONSTRUCTION.set(profile);
        try {
            return new SlayerMinionMob(ownerUuid, profile);
        } finally {
            IN_CONSTRUCTION.remove();
        }
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    @Override public String getDisplayName() { return live().displayName(); }
    @Override public Integer getLevel() { return live().level(); }
    @Override public List<MobType> getMobTypes() { return live().mobTypes(); }
    @Override public @Nullable SkyBlockLootTable getLootTable() { return null; }
    @Override public SkillCategories getSkillCategory() { return SkillCategories.COMBAT; }
    @Override public long damageCooldown() { return live().hooks().damageCooldownMillis(); }
    @Override public OtherLoot getOtherLoot() { return new OtherLoot(0, 0, 0); }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return live().hooks().stationary() ? List.of() : SlayerBossBehaviour.goals(this);
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return live().hooks().stationary() ? List.of() : SlayerBossBehaviour.targets(this);
    }

    @Override
    protected String nameHeader() {
        return live().hooks().nameHeader(this);
    }

    @Override
    public void attack(@org.jetbrains.annotations.NotNull net.minestom.server.entity.Entity target, boolean swingHand) {
        if (!live().hooks().canAttack(this)) {
            return;
        }
        super.attack(target, swingHand);
    }

    @Override
    public void onMeleeHit(dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer target) {
        live().hooks().onMeleeHit(this, target);
    }

    @Override
    public boolean damage(@org.jetbrains.annotations.NotNull Damage damage) {
        float amount = damage.getAmount();
        float modified = live().hooks().modifyIncomingDamage(this, damage, amount);
        if (modified <= 0F) {
            return false;
        }
        Damage applied = modified == amount
            ? damage
            : new Damage(damage.getType(), damage.getSource(), damage.getAttacker(), damage.getSourcePosition(), modified);
        return super.damage(applied);
    }

    @Override
    public void kill() {
        live().hooks().onDeath(this);
        super.kill();
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
            .withBase(ItemStatistic.HEALTH, live().health())
            .withBase(ItemStatistic.DAMAGE, live().damage())
            .withBase(ItemStatistic.SPEED, live().speed())
            .build();
    }

    private SlayerMinionProfile live() {
        return profile != null ? profile : IN_CONSTRUCTION.get();
    }

    public record SlayerMinionProfile(
        String displayName,
        int level,
        EntityType entityType,
        double health,
        double damage,
        double speed,
        List<MobType> mobTypes,
        Hooks hooks
    ) {
        public SlayerMinionProfile(String displayName, int level, EntityType entityType, double health, double damage,
                                   double speed, List<MobType> mobTypes) {
            this(displayName, level, entityType, health, damage, speed, mobTypes, Hooks.NONE);
        }
    }

    public interface Hooks {
        Hooks NONE = new Hooks() {
        };

        default boolean stationary() {
            return false;
        }

        default boolean canAttack(SlayerMinionMob minion) {
            return true;
        }

        default long damageCooldownMillis() {
            return 700L;
        }

        default String nameHeader(SlayerMinionMob minion) {
            return "";
        }

        default float modifyIncomingDamage(SlayerMinionMob minion, Damage damage, float amount) {
            return amount;
        }

        default void onMeleeHit(SlayerMinionMob minion, dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer target) {
        }

        default void onDeath(SlayerMinionMob minion) {
        }
    }
}
