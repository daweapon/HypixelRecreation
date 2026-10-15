package dev.daweapon.type.skyblockgeneric.fishing.lava;

import lombok.Getter;
import lombok.NonNull;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Crimson Isle lava Sea Creature. Stats, drops and Bestiary data come from {@link LavaSeaCreature}; the
 * creature travels in through {@link #begin} because SkyBlockMob's constructor reads them before fields are set.
 * Sea Creatures float on the lava, despawn after 6 minutes or when their angler leaves, and count toward the
 * angler's cap of 10.
 */
public class LavaSeaCreatureMob extends BestiaryMob {
    public static final int PER_PLAYER_CAP = 10;
    public static final String CAP_MESSAGE = "<c>There is not enough space for another Sea Creature! Kill some to make space for new ones!";
    private static final float AGGRO_RANGE = 20;
    private static final long DESPAWN_TICKS = 6 * 60 * 20;
    private static final ThreadLocal<Spawn> CONSTRUCTING = new ThreadLocal<>();
    private static final Map<UUID, Set<LavaSeaCreatureMob>> OWNED = new ConcurrentHashMap<>();

    private final Spawn spawn;
    private @Nullable Task ticker;
    @Getter
    private int ticksAlive;

    protected LavaSeaCreatureMob(LavaSeaCreature creature, @Nullable SkyBlockPlayer angler, boolean corrupted) {
        super(begin(new Spawn(creature, angler, corrupted && creature.canBeCorrupted())));
        this.spawn = CONSTRUCTING.get();
        CONSTRUCTING.remove();
        if (angler != null) OWNED.computeIfAbsent(angler.getUuid(), _ -> ConcurrentHashMap.newKeySet()).add(this);
        if (getCreature().getSpeed() <= 0 || floats()) setNoGravity(true);
    }

    private static net.minestom.server.entity.EntityType begin(Spawn spawn) {
        CONSTRUCTING.set(spawn);
        return spawn.creature().getEntityType();
    }

    public static int ownedBy(SkyBlockPlayer player) {
        Set<LavaSeaCreatureMob> owned = OWNED.get(player.getUuid());
        if (owned == null) return 0;
        owned.removeIf(mob -> mob.isRemoved() || mob.isDead());
        return owned.stream().mapToInt(LavaSeaCreatureMob::capWeight).sum();
    }

    protected int capWeight() {
        return 1;
    }

    private Spawn spec() {
        return spawn != null ? spawn : CONSTRUCTING.get();
    }

    public LavaSeaCreature getCreature() {
        return spec().creature();
    }

    public @Nullable SkyBlockPlayer getAngler() {
        return spec().angler();
    }

    public boolean isCorrupted() {
        return spec().corrupted();
    }

    protected boolean floats() {
        return getCreature() == LavaSeaCreature.LAVA_FLAME || getCreature() == LavaSeaCreature.FIRE_EEL
                || getCreature() == LavaSeaCreature.THUNDER;
    }

    protected boolean despawns() {
        return true;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        ticker = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (isRemoved() || isDead()) {
                stopTicking();
                return;
            }
            ticksAlive++;
            if (shouldDespawn()) {
                remove();
                return;
            }
            stayOnLavaSurface();
            onTick(ticksAlive);
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    private boolean shouldDespawn() {
        if (!despawns()) return false;
        if (ticksAlive >= DESPAWN_TICKS) return true;
        SkyBlockPlayer angler = getAngler();
        return angler != null && (!angler.isOnline() || angler.getInstance() != getInstance());
    }

    private void stayOnLavaSurface() {
        Instance instance = getInstance();
        if (instance == null || hasNoGravity()) return;
        if (instance.getBlock(getPosition().add(0, 0.2, 0)).name().contains("lava")) {
            setVelocity(getVelocity().withY(4));
        }
    }

    protected void onTick(int ticks) {
    }

    private void stopTicking() {
        if (ticker != null) ticker.cancel();
        ticker = null;
    }

    @Override
    public void remove() {
        stopTicking();
        super.remove();
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (damage.getAttacker() instanceof SkyBlockPlayer player && PeaceTreaty.applies(player)) return false;
        return super.damage(damage);
    }

    protected void hit(SkyBlockPlayer player, double amount) {
        if (amount <= 0 || player.isDead() || PeaceTreaty.applies(player)) return;
        MobAttackService.trueStrike(this, player, amount);
    }

    protected @Nullable SkyBlockPlayer killer() {
        return getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player
                ? player : null;
    }

    @Override
    public void kill() {
        SkyBlockPlayer killer = killer();
        stopTicking();
        super.kill();
        if (killer != null) onKilledBy(killer);
    }

    protected void onKilledBy(SkyBlockPlayer killer) {
    }

    protected List<SkyBlockLootTable.LootRecord> rollDrops() {
        List<SkyBlockLootTable.LootRecord> records = new ArrayList<>();
        for (LavaDrop drop : getCreature().getDrops()) {
            ItemType type = drop.itemType();
            if (type == null || (drop.enchantment() != null && LavaBooks.resolve(drop.enchantment()) == null)) continue;
            records.add(new SkyBlockLootTable.LootRecord(type, drop.rollAmount(), drop.chancePercent()));
        }
        if (isCorrupted()) records.addAll(CorruptedSeaCreatures.extraDrops());
        return records;
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        List<SkyBlockLootTable.LootRecord> records = rollDrops();
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
        SkyBlockItem drop = super.createDrop(type, amount);
        if (type == ItemType.ENCHANTED_BOOK) {
            getCreature().getDrops().stream().filter(row -> row.enchantment() != null).findFirst().ifPresent(row -> {
                EnchantmentType enchantment = LavaBooks.resolve(row.enchantment());
                if (enchantment != null) {
                    drop.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(enchantment, row.enchantmentLevel()));
                }
            });
        }
        return drop;
    }

    @Override
    public String getDisplayName() {
        return (isCorrupted() ? "<5>Corrupted " : "") + getCreature().getDisplayName();
    }

    @Override
    public Integer getLevel() {
        return getCreature().getLevel();
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return getCreature().getProfile().behaviour().goals(this);
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        if (getCreature().getDamage() <= 0) return List.of();
        return List.of(
                new LastEntityDamagerTarget(this, AGGRO_RANGE),
                new ClosestEntityTarget(this, AGGRO_RANGE,
                        entity -> entity instanceof SkyBlockPlayer player && !PeaceTreaty.applies(player)));
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        LavaSeaCreature creature = getCreature();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, creature.getHealth())
                .withBase(ItemStatistic.DAMAGE, creature.getDamage())
                .withBase(ItemStatistic.SPEED, creature.getSpeed())
                .build();
    }

    @Override
    public SkillCategories getSkillCategory() {
        return SkillCategories.FISHING;
    }

    @Override
    public long damageCooldown() {
        return 400;
    }

    @Override
    public OtherLoot getOtherLoot() {
        LavaSeaCreature creature = getCreature();
        return new OtherLoot(creature.getFishingXp(), creature.getCoins(), creature.getXpOrbs());
    }

    @Override
    public List<MobType> getMobTypes() {
        return getCreature().getMobTypes();
    }

    @Override
    public boolean isImmuneToArrows() {
        return true;
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
        return getCreature().name();
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return getCreature().getGuiMaterial();
    }

    protected List<SkyBlockPlayer> playersWithin(double range) {
        Instance instance = getInstance();
        if (instance == null) return List.of();
        Pos at = getPosition();
        return instance.getPlayers().stream()
                .filter(player -> player instanceof SkyBlockPlayer)
                .map(player -> (SkyBlockPlayer) player)
                .filter(player -> !player.isDead() && player.getPosition().distance(at) <= range)
                .toList();
    }

    record Spawn(LavaSeaCreature creature, @Nullable SkyBlockPlayer angler, boolean corrupted) {
    }
}
