package net.swofty.type.skyblockgeneric.mythological;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.swofty.type.skyblockgeneric.mythological.gear.MythologicalGear;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalAbilities;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalAbility;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
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
    private final MythologicalAbility ability;
    @Getter
    @Setter
    private boolean aiPaused;
    private boolean cleanedUp;
    private int abilityTicks;
    private @Nullable String twinName;
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
        this.ability = MythologicalAbilities.create(creature);
    }

    public static MythologicalCreatureMob of(MythologicalCreature creature, MythologicalVariant variant) {
        return new MythologicalCreatureMob(creature, variant);
    }

    private static net.minestom.server.entity.EntityType begin(Spec spec) {
        CONSTRUCTING.set(spec);
        return spec.creature().getEntityType();
    }

    private MythologicalAbility ability() {
        return ability != null ? ability : MythologicalAbility.NONE;
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
        first.twinName = "Bagheera";
        second.twinName = "Azrael";
        MythologicalAppearance.dressAsAzrael(second);
        first.refreshName();
        second.refreshName();
    }

    private boolean hasLivingTwin() {
        return twin != null && !twin.isDead() && !twin.isRemoved();
    }

    private boolean isHittable() {
        return !hasLivingTwin() || !twinsTurn;
    }

    @Override
    public void onInit() {
        MythologicalAppearance.dress(this);
    }

    @Override
    public void onSpawn() {
        spawnedAt = System.currentTimeMillis();
        ability().onSpawn(this);
    }

    public double rampagePercent() {
        double perSecond = getCreature().rampagePercentPerSecond();
        if (perSecond <= 0 || spawnedAt == 0) return 0;
        return Math.min(MAX_RAMPAGE_PERCENT, (System.currentTimeMillis() - spawnedAt) / 1000D * perSecond);
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (!isDead() && !isRemoved() && getInstance() != null) {
            ability().tick(this, abilityTicks);
            if (getCreature().rampagePercentPerSecond() > 0 && abilityTicks % 20 == 0) refreshName();
            abilityTicks++;
        }
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
            damage.setAmount((float) (damage.getAmount() * Carnivals.multiplier(player, CarnivalPerk.STORIED_STINGER)
                    * MythologicalGear.outgoingDamageMultiplier(player)));
        }
        float amount = ability().modifyIncomingDamage(this, damage, damage.getAmount());
        if (amount <= 0) return false;
        damage.setAmount(amount);
        boolean damaged = super.damage(damage);
        if (damaged) ability().onDamaged(this, damage, amount);
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
        cleanUp();
        super.kill();
        if (!escaped && listener != null) listener.onSlain(this);
    }

    @Override
    public void obliterate(SkyBlockPlayer player) {
        attackers.add(player.getUuid());
        super.obliterate(player);
    }

    @Override
    public void remove() {
        cleanUp();
        super.remove();
    }

    private void cleanUp() {
        if (cleanedUp) return;
        cleanedUp = true;
        aiPaused = true;
        ability().cleanup(this);
    }

    @Override
    public void onMeleeHit(SkyBlockPlayer target) {
        ability().onMeleeHit(this, target);
    }

    @Override
    protected String nameHeader() {
        String header = ability().nameHeader(this);
        if (!header.isEmpty()) return header;
        double rampage = rampagePercent();
        return Math.round(rampage) > 0 ? "<c>+" + Math.round(rampage) + "% damage" : "";
    }

    public int getFightTicks() {
        return abilityTicks;
    }

    public double healthRatio() {
        return getHealth() / getMaxHealth();
    }

    @Override
    public boolean isImmuneToArrows() {
        return ability().immuneToArrows();
    }

    @Override
    public ItemStatistics getStatistics() {
        ItemStatistics statistics = super.getStatistics();
        double rampage = rampagePercent();
        if (rampage > 0) {
            statistics = ItemStatistics.add(statistics, ItemStatistics.builder()
                    .withBase(ItemStatistic.DAMAGE, getVariant().damage() * rampage / 100D)
                    .build());
        }
        return ability().modifyStatistics(this, statistics);
    }

    @Override
    public String getDisplayName() {
        return getVariant().nameOf(twinName != null ? twinName : getCreature().getDisplayName());
    }

    @Override
    public Integer getLevel() {
        return getVariant().displayLevel();
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(MythologicalGoals.chase(this), MythologicalGoals.stroll(this));
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
    public SkyBlockItem createDrop(ItemType type, int amount) {
        SkyBlockItem drop = super.createDrop(type, amount);
        if (type == ItemType.ENCHANTED_BOOK) {
            drop.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.CHIMERA, 1));
        }
        return drop;
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
        return new GUIMaterial(getCreature().getIconTexture());
    }
}
