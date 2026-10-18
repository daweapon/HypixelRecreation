package dev.daweapon.type.skyblockgeneric.crimson.mob;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.time.TimeUnit;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategory;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.BlazingPalisade;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.FireDamageTaken;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Base for every Crimson Isle mob. Its numbers come from a {@link CrimsonMobProfile}, handed through a
 * thread-local because SkyBlockMob's constructor reads them before subclass fields exist. It tracks who
 * dealt how much damage, may spawn Corrupted, rolls its own drops, and costs faction reputation when killed.
 */
public abstract class CrimsonIsleMob extends BestiaryMob {
    public enum Temper { HOSTILE, NEUTRAL, PASSIVE }

    private static final ThreadLocal<CrimsonMobProfile> CONSTRUCTING = new ThreadLocal<>();

    private final CrimsonMobProfile profile;
    private final Map<UUID, Double> damageDealt = new ConcurrentHashMap<>();
    private @Nullable PendingHit pendingHit;
    @Getter
    private boolean corrupted;

    protected CrimsonIsleMob(CrimsonMobProfile profile) {
        super(begin(profile));
        this.profile = profile;
        CONSTRUCTING.remove();
    }

    private static EntityType begin(CrimsonMobProfile profile) {
        CONSTRUCTING.set(profile);
        return profile.entityType();
    }

    public CrimsonMobProfile getProfile() {
        return profile != null ? profile : CONSTRUCTING.get();
    }

    protected Temper temper() {
        return Temper.HOSTILE;
    }

    protected boolean melee() {
        return true;
    }

    protected float aggroRange() {
        return 16;
    }

    protected int strollRadius() {
        return 6;
    }

    protected boolean canTarget(SkyBlockPlayer player) {
        return true;
    }

    public boolean countsTowardVanquisher() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return (corrupted ? "<5>Corrupted " : "") + getProfile().displayName();
    }

    @Override public Integer getLevel() { return getProfile().level(); }
    @Override public ItemStatistics getBaseStatistics() { return getProfile().statistics(); }
    @Override public OtherLoot getOtherLoot() { return getProfile().otherLoot(); }
    @Override public List<MobType> getMobTypes() { return getProfile().mobTypes(); }
    @Override public SkillCategories getSkillCategory() { return SkillCategories.COMBAT; }
    @Override public long damageCooldown() { return 500; }
    @Override public @Nullable SkyBlockLootTable getLootTable() { return null; }
    @Override public int getMaxBestiaryTier() { return getProfile().maxTier(); }
    @Override public int getBestiaryBracket() { return getProfile().bracket(); }
    @Override public String getMobID() { return getProfile().id(); }
    @Override public boolean countsForBestiary() { return BestiaryFamilies.IDS.contains(getMobID()); }
    @Override public GUIMaterial getGuiMaterial() { return getProfile().guiMaterial(); }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        List<GoalSelector> goals = new ArrayList<>();
        if (temper() != Temper.PASSIVE && melee()) {
            goals.add(new VanillaMeleeAttackGoal(this, 2.2, 20, TimeUnit.SERVER_TICK));
        }
        if (getProfile().speed() > 0 && strollRadius() > 0) goals.add(new VanillaRandomStrollGoal(this, strollRadius()));
        return goals;
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return switch (temper()) {
            case PASSIVE -> List.of();
            case NEUTRAL -> List.of(new LastEntityDamagerTarget(this, aggroRange()),
                    new ClosestEntityTarget(this, aggroRange(), this::isProvokingPlayer));
            case HOSTILE -> List.of(new LastEntityDamagerTarget(this, aggroRange()),
                    new ClosestEntityTarget(this, aggroRange(),
                            entity -> entity instanceof SkyBlockPlayer player && canTarget(player)));
        };
    }

    private boolean isProvokingPlayer(Entity entity) {
        return entity instanceof SkyBlockPlayer player && (isProvokedBy(player) || canTarget(player));
    }

    public boolean isProvokedBy(SkyBlockPlayer player) {
        return damageDealt.containsKey(player.getUuid());
    }

    public double damageDealtBy(UUID player) {
        return damageDealt.getOrDefault(player, 0D);
    }

    public Map<UUID, Double> getDamageDealt() {
        return damageDealt;
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (!(damage.getAttacker() instanceof SkyBlockPlayer player)) return super.damage(damage);
        if (!acceptsDamageFrom(player, damage)) return false;
        PendingHit outer = pendingHit;
        pendingHit = new PendingHit(player.getUuid(), getHealth());
        boolean hurt;
        try {
            hurt = super.damage(damage);
        } finally {
            creditPendingHit();
            pendingHit = outer;
        }
        if (hurt) onDamagedBy(player, damage);
        return hurt;
    }

    private record PendingHit(UUID player, float healthBefore) {
    }

    private void creditPendingHit() {
        PendingHit hit = pendingHit;
        if (hit == null) return;
        pendingHit = null;
        credit(hit.player(), hit.healthBefore() - getHealth());
    }

    public void credit(UUID player, double damage) {
        if (damage > 0) damageDealt.merge(player, damage, Double::sum);
    }

    @Override
    public void obliterate(SkyBlockPlayer player) {
        if (isDead() || isRemoved()) return;
        credit(player.getUuid(), getHealth());
        super.obliterate(player);
    }

    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        return true;
    }

    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
    }

    /** Quest spawns that must be Corrupted, such as the Barbarian questline's Corrupted Flaming Spider. */
    public void corrupt() {
        corrupted = true;
        refreshName();
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        if (CorruptedMobs.rollOnSpawn(getProfile())) {
            corrupted = true;
            refreshName();
        }
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (isDead() || isRemoved() || getInstance() == null) return;
        onAiTick((int) getAliveTicks());
    }

    protected void onAiTick(int ticks) {
    }

    protected @Nullable SkyBlockPlayer killer() {
        return getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player
                ? player : null;
    }

    @Override
    public void kill() {
        creditPendingHit();
        SkyBlockPlayer killer = killer();
        super.kill();
        if (killer == null) return;
        CrimsonMobLoot.roll(killer, this, drops());
        if (getProfile().faction() != null && getProfile().reputationLoss() > 0) {
            CrimsonReputationService.add(killer, getProfile().faction(), -getProfile().reputationLoss(), null);
        }
        onKilledBy(killer);
    }

    protected List<MobDrop> drops() {
        if (!corrupted) return getProfile().drops();
        List<MobDrop> drops = new ArrayList<>(getProfile().drops());
        drops.addAll(CorruptedMobs.DROPS);
        return drops;
    }

    protected void onKilledBy(SkyBlockPlayer killer) {
    }

    public List<SkyBlockPlayer> playersWithin(double range) {
        Instance instance = getInstance();
        if (instance == null) return List.of();
        Pos at = getPosition();
        return instance.getPlayers().stream()
                .filter(player -> player instanceof SkyBlockPlayer && !player.isDead())
                .map(player -> (SkyBlockPlayer) player)
                .filter(player -> player.getPosition().distance(at) <= range)
                .toList();
    }

    public Optional<SkyBlockPlayer> nearestPlayer(double range, Predicate<SkyBlockPlayer> filter) {
        return playersWithin(range).stream().filter(filter)
                .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(getPosition())));
    }

    protected Optional<SkyBlockPlayer> rangedTarget(double range) {
        return switch (temper()) {
            case PASSIVE -> Optional.empty();
            case NEUTRAL -> nearestPlayer(range, player -> isProvokedBy(player) || canTarget(player));
            case HOSTILE -> nearestPlayer(range, this::canTarget);
        };
    }

    protected void fireball(SkyBlockPlayer target, double damage) {
        MobProjectile.item(this, target, ItemStack.of(Material.FIRE_CHARGE), 0.9, hit -> {
            if (BlazingPalisade.reflect(hit, this, damage)) return;
            float dealt = MobAttackService.strike(this, hit, damage * FireDamageTaken.multiplier(hit));
            FireDamageTaken.record(hit, dealt);
        });
    }

    protected void strike(SkyBlockPlayer player, double damage) {
        if (player.isDead()) return;
        MobAttackService.strike(this, player, damage);
    }

    protected void trueStrike(SkyBlockPlayer player, double damage) {
        if (player.isDead()) return;
        MobAttackService.trueStrike(this, player, damage);
    }

    private static final class BestiaryFamilies {
        private static final Set<String> IDS = Arrays.stream(BestiaryCategory.CRIMSON_ISLE.values())
                .flatMap(family -> family.getMobs().stream())
                .map(BestiaryMob::getMobID)
                .collect(Collectors.toUnmodifiableSet());
    }
}
