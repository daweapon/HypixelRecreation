package net.swofty.type.skyblockgeneric.item.set.sets;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.user.statistics.TemporaryStatistic;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.armor.ArmorAbilities;
import net.swofty.type.skyblockgeneric.item.armor.ArmorWorn;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import net.swofty.type.skyblockgeneric.item.set.impl.SetRepeatable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorPieceEffects implements SetRepeatable {
    public static final int PERIOD_TICKS = 5;
    private static final Map<UUID, Integer> BONES = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> BONE_REGEN = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> UNSTABLE_TIMER = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> EGG_COOLDOWN = new ConcurrentHashMap<>();
    private static final Set<UUID> SNEAKING = ConcurrentHashMap.newKeySet();
    private static final Set<String> SALMON_HEADS = Set.of("SALMON_HELMET", "SALMON_HAT", "SALMON_HELMET_NEW", "SALMON_HAT_CELEBRATION");
    private static final Map<UUID, Long> ENRAGE_READY = new ConcurrentHashMap<>();
    private int tick;

    public static boolean consumeBone(SkyBlockPlayer player) {
        int bones = BONES.computeIfAbsent(player.getUuid(), _ -> ArmorAbilities.BONE_SHIELD_MAX);
        if (bones <= 0) return false;
        BONES.put(player.getUuid(), bones - 1);
        return true;
    }

    public static boolean wearsSalmonSet(SkyBlockPlayer player) {
        return SALMON_HEADS.contains(ArmorWorn.idOf(player.getHelmet()))
                && "SALMON_CHESTPLATE".equals(ArmorWorn.idOf(player.getChestplate()))
                && "SALMON_LEGGINGS".equals(ArmorWorn.idOf(player.getLeggings()))
                && "SALMON_BOOTS".equals(ArmorWorn.idOf(player.getBoots()));
    }

    @Override
    public Task getTask(Scheduler scheduler) {
        return scheduler.buildTask(() -> {
            tick += PERIOD_TICKS;
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getInstance() == null || player.isDead()) continue;
                try {
                    tickPlayer(player);
                } catch (RuntimeException ignored) {
                }
            }
        }).repeat(TaskSchedule.tick(PERIOD_TICKS)).schedule();
    }

    private void tickPlayer(SkyBlockPlayer player) {
        boolean justSneaked = player.isSneaking() ? SNEAKING.add(player.getUuid()) : SNEAKING.remove(player.getUuid()) && false;
        String helmet = ArmorWorn.idOf(player.getHelmet());
        if ("RABBIT_HAT".equals(helmet)) {
            player.addEffect(new Potion(PotionEffect.JUMP_BOOST, (byte) 3, 20));
        }
        if ("COW_HEAD".equals(helmet)) clearDebuffs(player);
        if ("SKELETON_HELMET".equals(helmet)) regenerateBones(player);
        if (justSneaked) {
            if (wearsSalmonSet(player)) waterBurst(player);
            if ("CHICKEN_HEAD".equals(helmet)) layEgg(player);
            if (ArmorWorn.countPrefix(player, "REAPER") >= ArmorAbilities.TROLLING_PIECES) enrage(player);
        }
        EGG_COOLDOWN.computeIfPresent(player.getUuid(), (_, left) -> left > PERIOD_TICKS ? left - PERIOD_TICKS : null);
        if (tick % 20 == 0 && ArmorWorn.fullSet(player, "FROZEN_BLAZE")) frozenBlazingAura(player);
        if (ArmorWorn.fullSet(player, "UNSTABLE_DRAGON")) unstableBlood(player);
    }

    private void clearDebuffs(SkyBlockPlayer player) {
        for (TimedPotion active : List.copyOf(player.getActiveEffects())) {
            PotionEffect effect = active.potion().effect();
            if (ArmorAbilities.isNegative(effect.name().replace("minecraft:", ""))) player.removeEffect(effect);
        }
    }

    private void regenerateBones(SkyBlockPlayer player) {
        int bones = BONES.computeIfAbsent(player.getUuid(), _ -> ArmorAbilities.BONE_SHIELD_MAX);
        if (bones >= ArmorAbilities.BONE_SHIELD_MAX) {
            BONE_REGEN.remove(player.getUuid());
            return;
        }
        int elapsed = BONE_REGEN.merge(player.getUuid(), PERIOD_TICKS, Integer::sum);
        if (elapsed >= ArmorAbilities.BONE_SHIELD_REGEN_TICKS) {
            BONES.put(player.getUuid(), bones + 1);
            BONE_REGEN.remove(player.getUuid());
        }
    }

    private void waterBurst(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (!instance.getBlock(player.getPosition()).compare(Block.WATER)) return;
        int cost = RegisteredAbility.effectiveManaCost(player, ArmorAbilities.WATER_BURST_MANA);
        if (player.getMana() < cost) return;
        player.setMana(player.getMana() - cost);
        Vec direction = player.getPosition().direction();
        player.setVelocity(direction.mul(ArmorAbilities.WATER_BURST_SPEED));
    }

    private void enrage(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        if (ENRAGE_READY.getOrDefault(player.getUuid(), 0L) > now) return;
        ENRAGE_READY.put(player.getUuid(), now + ArmorAbilities.ENRAGE_COOLDOWN_MS);
        ItemStatistics stats = ItemStatistics.builder()
                .withBase(ItemStatistic.SPEED, ArmorAbilities.ENRAGE_STAT)
                .withBase(ItemStatistic.DAMAGE, ArmorAbilities.ENRAGE_STAT)
                .withBase(ItemStatistic.STRENGTH, ArmorAbilities.ENRAGE_STAT).build();
        player.getStatistics().boostStatistic(TemporaryStatistic.builder().withStatistics(stats)
                .withExpirationInMs(ArmorAbilities.ENRAGE_DURATION_MS).withDisplayName("Enrage").build());
    }

    private void layEgg(SkyBlockPlayer player) {
        if (EGG_COOLDOWN.containsKey(player.getUuid())) return;
        EGG_COOLDOWN.put(player.getUuid(), ArmorAbilities.EGG_COOLDOWN_TICKS);
        ItemEntity egg = new ItemEntity(ItemStack.of(Material.EGG));
        egg.setPickable(false);
        egg.setMergeable(false);
        egg.setInstance(player.getInstance(), player.getPosition());
        egg.scheduleRemove(Duration.ofSeconds(10));
    }

    private void frozenBlazingAura(SkyBlockPlayer player) {
        ItemType rod = ItemType.get("BLAZE_ROD");
        long rods = 0;
        if (rod != null) {
            Integer collected = player.getCollection().get(rod);
            rods = collected == null ? 0 : collected;
        }
        for (SkyBlockMob mob : mobsNear(player, ArmorAbilities.FROZEN_BLAZE_RANGE)) {
            double damage = ArmorAbilities.frozenBlazeDamage(mob.getMaxHealth(), rods);
            if (AttackService.applyHit(player, mob, (float) damage, false)) {
                mob.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 0, 80));
            }
        }
    }

    private void unstableBlood(SkyBlockPlayer player) {
        int elapsed = UNSTABLE_TIMER.merge(player.getUuid(), PERIOD_TICKS, Integer::sum);
        if (elapsed < ArmorAbilities.UNSTABLE_INTERVAL_TICKS) return;
        UNSTABLE_TIMER.put(player.getUuid(), 0);
        if (player.isOnIsland()) return;
        for (SkyBlockMob mob : mobsNear(player, ArmorAbilities.UNSTABLE_RANGE)) {
            if (!AttackService.applyHit(player, mob, (float) ArmorAbilities.UNSTABLE_DAMAGE, false)) continue;
            Entity bolt = new Entity(EntityType.LIGHTNING_BOLT);
            bolt.setInstance(mob.getInstance(), mob.getPosition());
            bolt.scheduleRemove(Duration.ofSeconds(1));
        }
    }

    public static List<SkyBlockMob> mobsNear(SkyBlockPlayer player, double range) {
        return player.getInstance().getNearbyEntities(player.getPosition(), range).stream()
                .filter(entity -> entity instanceof SkyBlockMob mob && !mob.isDead() && !mob.isRemoved())
                .map(entity -> (SkyBlockMob) entity)
                .toList();
    }
}
