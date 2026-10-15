package net.swofty.type.skyblockgeneric.crystalhollows.mob;

import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
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
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.MiningPetBonuses;
import net.swofty.type.skyblockgeneric.loottable.LootAffector;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.MobFreeZones;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public abstract class CrystalHollowsMob extends BestiaryMob {
    private static final int MOB_FREE_CHECK_TICKS = 40;

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
    public void tick(long time) {
        super.tick(time);
        if (isDead() || getInstance() == null) return;
        if (getAliveTicks() % MOB_FREE_CHECK_TICKS == 0 && MobFreeZones.isMobFreeAt(getPosition())) {
            remove();
            return;
        }
        onAiTick((int) getAliveTicks());
    }

    protected void onAiTick(int ticks) {
    }

    protected void onHitBy(SkyBlockPlayer player) {
    }

    protected Optional<SkyBlockPlayer> nearestPlayer(double range) {
        if (getInstance() == null) return Optional.empty();
        return getInstance().getNearbyEntities(getPosition(), range).stream()
                .filter(entity -> entity instanceof SkyBlockPlayer player && !player.isDead())
                .map(entity -> (SkyBlockPlayer) entity)
                .min(java.util.Comparator.comparingDouble(player -> player.getPosition().distanceSquared(getPosition())));
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        boolean hurt = super.damage(damage);
        if (hurt && damage.getAttacker() instanceof SkyBlockPlayer player) onHitBy(player);
        return hurt;
    }

    @Override
    public void kill() {
        super.kill();
        if (!(getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player)) return;
        rollExclusiveDrops(player);
    }

    private void rollExclusiveDrops(SkyBlockPlayer player) {
        List<CrystalHollowsMobSpec.Drop> exclusive = spec().exclusiveDrops();
        if (exclusive.isEmpty()) return;
        List<ExclusiveEntry> entries = new ArrayList<>();
        double taken = 0;
        for (CrystalHollowsMobSpec.Drop drop : exclusive) {
            double chance = MiningPetBonuses.automatonPartChance(player,
                    LootAffector.MAGIC_FIND.getAffector().apply(player, drop.chance(), this), this);
            entries.add(new ExclusiveEntry(drop, chance));
            taken += chance;
        }
        entries.add(new ExclusiveEntry(null, Math.max(0, 100 - taken)));
        ExclusiveEntry picked = LootRoll.pick(player.getUuid(), entries,
                entry -> entry.drop() == null ? null : entry.drop().item(), ExclusiveEntry::weight,
                ThreadLocalRandom.current());
        if (picked.drop() == null) return;
        awardDrop(player, picked.drop().item(),
                new SkyBlockLootTable.LootRecord(picked.drop().item(), picked.drop().min(), picked.drop().chance()));
    }

    private record ExclusiveEntry(CrystalHollowsMobSpec.Drop drop, double weight) {
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
