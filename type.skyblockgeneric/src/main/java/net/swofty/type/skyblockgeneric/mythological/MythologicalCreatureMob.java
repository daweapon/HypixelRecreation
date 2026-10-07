package net.swofty.type.skyblockgeneric.mythological;

import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import lombok.Getter;
import lombok.NonNull;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import net.swofty.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * One Mythological creature dug out of a Griffin Burrow. The creature and the Griffin rarity it was
 * dug with travel through {@link #begin} because SkyBlockMob's constructor reads stats before this
 * class can assign fields.
 */
public class MythologicalCreatureMob extends BestiaryMob {
    private static final ThreadLocal<Spec> CONSTRUCTING = new ThreadLocal<>();
    private static final double MAX_RAMPAGE_PERCENT = 600;
    private static final long WARNING_COOLDOWN_MS = 2000;
    private static final int TWIN_PARTICLE_INTERVAL_TICKS = 5;

    private record Spec(MythologicalCreature creature, MythologicalVariant variant) {
    }

    public interface Listener {
        void onSlain(MythologicalCreatureMob mob);

        void onEscaped(MythologicalCreatureMob mob);
    }

    private final Spec spec;
    @Getter
    private final Set<UUID> attackers = new LinkedHashSet<>();
    private final Map<UUID, Long> lastWarning = new HashMap<>();
    @Getter
    private @Nullable UUID digger;
    private @Nullable Listener listener;
    private long spawnedAt;
    private boolean escaped;
    private @Nullable MythologicalCreatureMob twin;
    private boolean twinsTurn;
    private int ticksAlive;

    protected MythologicalCreatureMob(MythologicalCreature creature, MythologicalVariant variant) {
        super(begin(new Spec(creature, variant)));
        this.spec = CONSTRUCTING.get();
        CONSTRUCTING.remove();
    }

    public static MythologicalCreatureMob of(MythologicalCreature creature, MythologicalVariant variant) {
        return new MythologicalCreatureMob(creature, variant);
    }

    private static net.minestom.server.entity.EntityType begin(Spec spec) {
        CONSTRUCTING.set(spec);
        return spec.creature().getEntityType();
    }

    private Spec spec() {
        return spec != null ? spec : CONSTRUCTING.get();
    }

    public MythologicalCreature getCreature() {
        return spec().creature();
    }

    public MythologicalVariant getVariant() {
        return spec().variant();
    }

    public void bind(UUID digger, Listener listener) {
        this.digger = digger;
        this.listener = listener;
    }

    public static void pairAsTwins(MythologicalCreatureMob first, MythologicalCreatureMob second) {
        first.twin = second;
        second.twin = first;
        second.twinsTurn = true;
    }

    private boolean hasLivingTwin() {
        return twin != null && !twin.isDead() && !twin.isRemoved();
    }

    private boolean isHittable() {
        return !hasLivingTwin() || !twinsTurn;
    }

    @Override
    public void onSpawn() {
        spawnedAt = System.currentTimeMillis();
    }

    public double rampagePercent() {
        double perSecond = getCreature().rampagePercentPerSecond();
        if (perSecond <= 0 || spawnedAt == 0) return 0;
        return Math.min(MAX_RAMPAGE_PERCENT, (System.currentTimeMillis() - spawnedAt) / 1000D * perSecond);
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (!escaped && !isDead() && rampagePercent() >= MAX_RAMPAGE_PERCENT) {
            escaped = true;
            if (listener != null) listener.onEscaped(this);
            remove();
        }
        if (hasLivingTwin() && !twinsTurn && !isDead() && ticksAlive++ % TWIN_PARTICLE_INTERVAL_TICKS == 0) {
            sendPacketToViewers(new ParticlePacket(Particle.ANGRY_VILLAGER, false, true,
                    (float) getPosition().x(), (float) (getPosition().y() + getEyeHeight() + 0.6), (float) getPosition().z(),
                    0.2f, 0.1f, 0.2f, 0, 1));
        }
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (damage.getAttacker() instanceof SkyBlockPlayer player) {
            Rarity required = getVariant().griffinRarity();
            boolean qualified = GriffinPet.equippedRarity(player).map(rarity -> rarity.isAtLeast(required)).orElse(false);
            if (!qualified) {
                warn(player, required);
                return false;
            }
            if (!isHittable()) return false;
            attackers.add(player.getUuid());
            damage.setAmount((float) (damage.getAmount() * Carnivals.multiplier(player, CarnivalPerk.STORIED_STINGER)));
        }
        boolean damaged = super.damage(damage);
        if (damaged && damage.getAttacker() instanceof SkyBlockPlayer && hasLivingTwin()) {
            twinsTurn = true;
            twin.twinsTurn = false;
        }
        return damaged;
    }

    private void warn(SkyBlockPlayer player, Rarity required) {
        long now = System.currentTimeMillis();
        Long last = lastWarning.get(player.getUuid());
        if (last != null && now - last < WARNING_COOLDOWN_MS) return;
        lastWarning.put(player.getUuid(), now);
        player.sendMessage("<c>You need a " + GriffinPet.coloredName(required) + " <c>or higher Griffin equipped to damage this creature!");
    }

    @Override
    public void kill() {
        super.kill();
        if (!escaped && listener != null) listener.onSlain(this);
    }

    @Override
    public ItemStatistics getStatistics() {
        ItemStatistics statistics = super.getStatistics();
        double rampage = rampagePercent();
        if (rampage <= 0) return statistics;
        return ItemStatistics.add(statistics, ItemStatistics.builder()
                .withBase(ItemStatistic.DAMAGE, getVariant().damage() * rampage / 100D)
                .build());
    }

    @Override
    public String getDisplayName() {
        return getVariant().nameOf(getCreature().getDisplayName());
    }

    @Override
    public Integer getLevel() {
        return getVariant().level();
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(
                new VanillaMeleeAttackGoal(this, 1.6, 20, TimeUnit.SERVER_TICK),
                new VanillaRandomStrollGoal(this, 6));
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of(
                new LastEntityDamagerTarget(this, 24),
                new ClosestEntityTarget(this, 16, entity -> entity instanceof SkyBlockPlayer));
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, getVariant().health())
                .withBase(ItemStatistic.DAMAGE, getVariant().damage())
                .withBase(ItemStatistic.SPEED, getCreature().getSpeed())
                .build();
    }

    @Override
    public SkyBlockLootTable getLootTable() {
        List<SkyBlockLootTable.LootRecord> records = getVariant().drops().stream()
                .filter(MythologicalDrop::isAvailable)
                .map(drop -> new SkyBlockLootTable.LootRecord(drop.item(), drop.amount(), drop.chancePercent()))
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
    public SkillCategories getSkillCategory() {
        return SkillCategories.COMBAT;
    }

    @Override
    public long damageCooldown() {
        return 500;
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(getVariant().combatXp(), getVariant().coins(), getVariant().orbs());
    }

    @Override
    public List<MobType> getMobTypes() {
        return getCreature().getMobTypes();
    }

    @Override
    public int getMaxBestiaryTier() {
        return getCreature().getBestiary().maxTier();
    }

    @Override
    public int getBestiaryBracket() {
        return getCreature().getBestiary().bracket();
    }

    @Override
    public String getMobID() {
        return getVariant().mobId();
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return new GUIMaterial(getCreature().getIcon());
    }
}
