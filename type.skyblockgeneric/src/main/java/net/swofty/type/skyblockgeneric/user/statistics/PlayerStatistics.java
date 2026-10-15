package net.swofty.type.skyblockgeneric.user.statistics;

import net.swofty.type.skyblockgeneric.accessory.AccessoryPower;
import net.swofty.type.skyblockgeneric.accessory.power.AccessoryPowers;

import net.swofty.type.skyblockgeneric.accessory.AccessoryBagXP;
import net.swofty.type.skyblockgeneric.accessory.AccessoryOwnership;
import net.swofty.type.skyblockgeneric.accessory.AccessoryStats;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeRuneInfusedWith;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryData;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointInventory;
import net.swofty.type.skyblockgeneric.elections.perks.ActivePerks;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.elections.perks.MayorPerkStatistics;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.enchantment.debuff.LethalityDebuff;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.ConstantStatisticsComponent;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointPetData;
import net.swofty.type.skyblockgeneric.item.components.StandardItemComponent;
import net.swofty.type.skyblockgeneric.accessory.hatcessory.Hatcessories;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.set.impl.TieredArmorSet;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.carnival.CarnivalStatistics;
import net.swofty.type.skyblockgeneric.mythological.gear.MythologicalGear;
import net.swofty.type.skyblockgeneric.user.pets.PetItems;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import net.swofty.type.skyblockgeneric.region.rules.ZoneRuleService;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Predicate;

public class PlayerStatistics {
    private final SkyBlockPlayer player;
    @Setter
    @Getter
    private double healthRegenerationPercentBonus;
    @Setter
    @Getter
    private double manaRegenerationPercentBonus;
    @Getter
    private double incomingDamageReductionPercent;
    private ItemStatistics accessoryStatistics = ItemStatistics.builder().build();
    private ItemStatistics thaumaturgyStatistics = ItemStatistics.empty();
    // Pet ability conditions may read stats (e.g. Griffin's "above 85% Health" reads max health),
    // which would recurse back into the pet's own statistics.
    private final ThreadLocal<Boolean> computingPetStatistics = ThreadLocal.withInitial(() -> false);
    private final List<TemporaryStatistic> temporaryStatistics = Collections.synchronizedList(new ArrayList<>());
    private final List<TemporaryConditionalStatistic> temporaryConditionalStatistics = Collections.synchronizedList(new ArrayList<>());
    BestiaryData bestiaryData = new BestiaryData();
    private volatile CachedStatistics cachedAllStatistics;

    /**
     * Health, defense, mana, speed and the action bar each read the full stat total several times a
     * tick; the total is reused for one tick instead of rebuilding every item each time.
     */
    private static final long ALL_STATISTICS_REUSE_NANOS = 50_000_000L;

    private record CachedStatistics(ItemStatistics statistics, long computedAt) {
    }

    private final ItemStatisticsCalculator itemCalculator;
    private final StatisticModifierCollector modifierCollector;

    public PlayerStatistics(SkyBlockPlayer player) {
        this.player = player;
        this.itemCalculator = new ItemStatisticsCalculator(player);
        this.modifierCollector = new StatisticModifierCollector(player, this, itemCalculator,
                temporaryStatistics, temporaryConditionalStatistics);
    }

    public @Nullable SkyBlockItem getItemWithRune(ItemType runeType) {
        List<SkyBlockItem> piecesToCheck = getPossibleRuneItems();

        for (SkyBlockItem item : piecesToCheck) {
            if (item == null) continue;
            ItemAttributeRuneInfusedWith.RuneData runeData = item.getAttributeHandler().getRuneData();
            if (!runeData.hasRune()) continue;
            if (runeData.getRuneType() != runeType) continue;

            return item;
        }

        return null;
    }

    public List<SkyBlockItem> getRunedItems() {
        return getPossibleRuneItems().stream().filter(Objects::nonNull).toList();
    }

    private List<SkyBlockItem> getPossibleRuneItems() {
        ArrayList<SkyBlockItem> piecesToCheck = new ArrayList<>();
        PlayerItemOrigin.OriginCache cache = PlayerItemOrigin.getFromCache(player.getUuid());

        piecesToCheck.add(getMainHandItem());
        piecesToCheck.add(cache.get(PlayerItemOrigin.HELMET));
        piecesToCheck.add(cache.get(PlayerItemOrigin.CHESTPLATE));
        piecesToCheck.add(cache.get(PlayerItemOrigin.LEGGINGS));
        piecesToCheck.add(cache.get(PlayerItemOrigin.BOOTS));

        return piecesToCheck;
    }

    public ItemStatistics allArmorStatistics(SkyBlockPlayer causer, LivingEntity enemy) {
        PlayerItemOrigin.OriginCache cache = PlayerItemOrigin.getFromCache(player.getUuid());
        ArrayList<SkyBlockItem> armorPieces = new ArrayList<>();

        armorPieces.add(cache.get(PlayerItemOrigin.HELMET));
        armorPieces.add(cache.get(PlayerItemOrigin.CHESTPLATE));
        armorPieces.add(cache.get(PlayerItemOrigin.LEGGINGS));
        armorPieces.add(cache.get(PlayerItemOrigin.BOOTS));

        ItemStatistics total = ItemStatistics.builder().build();
        for (SkyBlockItem item : armorPieces) {
            if (item.hasComponent(ConstantStatisticsComponent.class))
                continue;

            total = ItemStatistics.add(total, ItemStatistics.add(item.getAttributeHandler().getStatistics(), calculateExtraItemStatisticsToAdd(
                    item,
                    causer,
                    enemy
            )));
        }
        if (player.getArmorSet() != null) {
            ArmorSetRegistry armorSetRegistry = player.getArmorSet();
            try {
                Constructor<? extends ArmorSet> constructor = armorSetRegistry.getClazz().getConstructor();
                ArmorSet armorSet = constructor.newInstance();
                total = ItemStatistics.add(total, armorSet.getStatistics(player));
            } catch (Exception _) {
            }
        }
        total = ItemStatistics.add(total, Hatcessories.wornStatistics(player, cache.get(PlayerItemOrigin.HELMET)));
        total = ItemStatistics.add(total, net.swofty.type.skyblockgeneric.item.armor.ArmorAbilityEffects.setStatistics(player));
        return ItemStatistics.add(total, TieredArmorSet.statistics(player));
    }

    // Returns a map of Level : Amount of that level
    public @NonNull Map<Integer, Integer> getAllOfEnchants(EnchantmentType enchantmentType,
                                                           boolean includeAccessories) {
        List<SkyBlockItem> toCheck = new ArrayList<>();
        PlayerItemOrigin.OriginCache cache = PlayerItemOrigin.getFromCache(player.getUuid());

        toCheck.add(getMainHandItem());
        toCheck.add(cache.get(PlayerItemOrigin.HELMET));
        toCheck.add(cache.get(PlayerItemOrigin.CHESTPLATE));
        toCheck.add(cache.get(PlayerItemOrigin.LEGGINGS));
        toCheck.add(cache.get(PlayerItemOrigin.BOOTS));

        if (includeAccessories) {
            toCheck.addAll(player.getAccessoryBag().getAllAccessories());
        }

        Map<Integer, Integer> enchantLevels = new HashMap<>();

        for (SkyBlockItem item : toCheck) {
            if (item == null) continue;

            for (SkyBlockEnchantment enchantment : item.getAttributeHandler().getEnchantments().toList()) {
                if (enchantment.type() != enchantmentType) continue;

                int level = enchantment.level();
                enchantLevels.merge(level, 1, Integer::sum);
            }
        }

        return enchantLevels;
    }

    public ItemStatistics mainHandStatistics(SkyBlockPlayer causer, LivingEntity enemy) {
        SkyBlockItem item = getMainHandItem();

        if (item.hasComponent(ConstantStatisticsComponent.class))
            return ItemStatistics.empty();
        if (item.hasComponent(StandardItemComponent.class)) {
            StandardItemComponent standardItem = item.getComponent(StandardItemComponent.class);
            if (standardItem.getType().isArmor())
                return ItemStatistics.empty();
        }

        return ItemStatistics.add(item.getAttributeHandler().getStatistics(),
                calculateExtraItemStatisticsToAdd(
                        item,
                        causer,
                        enemy
                ));
    }

    public ItemStatistics petStatistics() {
        if (computingPetStatistics.get()) return ItemStatistics.empty();
        computingPetStatistics.set(true);
        try {
            return computePetStatistics();
        } finally {
            computingPetStatistics.set(false);
        }
    }

    private ItemStatistics computePetStatistics() {
        if (player.getPetData().isSuspended()) return ItemStatistics.empty();
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        ItemStatistics stats = pet == null ? ItemStatistics.empty() : PetItems.statistics(pet);

        for (DatapointPetData.ActiveAbility active : player.getPetData().getActiveAbilities()) {
            stats = ItemStatistics.add(stats, active.ability().getStatistics(player, active.pet()));
        }
        stats = ItemStatistics.add(stats, net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetAuras.statistics(player));

        return stats;
    }

    public ItemStatistics spareStatistics() {
        return new SpareStatistics(player).statistics();
    }

    public long getInvulnerabilityTime() {
        double bonusAttackSpeed = allStatistics().getOverall(ItemStatistic.BONUS_ATTACK_SPEED);
        return (long) (10 / (1 + (bonusAttackSpeed / 100)));
    }


    public ItemStatistics allStatistics() {
        long now = System.nanoTime();
        CachedStatistics cached = cachedAllStatistics;
        if (cached != null && now - cached.computedAt() < ALL_STATISTICS_REUSE_NANOS) return cached.statistics();
        ItemStatistics computed = allStatistics(null, null);
        cachedAllStatistics = new CachedStatistics(computed, now);
        return computed;
    }

    public ItemStatistics allStatistics(SkyBlockPlayer causer, LivingEntity enemy) {
        ItemStatistics total = ItemStatistics.builder().build();
        if (enemy instanceof BestiaryMob bestiaryMob) total = ItemStatistics.add(total, getBestiaryStatistics(causer, bestiaryMob));
        total = ItemStatistics.add(total, CarnivalStatistics.against(player, enemy));
        total = ItemStatistics.add(total, MythologicalGear.against(player, enemy));
        total = ItemStatistics.add(total, net.swofty.type.skyblockgeneric.fishing.lava.CrimsonFishingGear.against(player, enemy));
        total = ItemStatistics.add(total, net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.GatheringPetBonuses.against(player, enemy));
        total = ItemStatistics.add(total, net.swofty.type.skyblockgeneric.item.armor.ArmorAbilityEffects.againstStatistics(player, enemy));
        total = ItemStatistics.add(total, allArmorStatistics(causer, enemy));
        total = ItemStatistics.add(total, equipmentStatistics(causer, enemy));
        total = ItemStatistics.add(total, net.swofty.type.skyblockgeneric.item.equipment.EquipmentBonuses.statistics(player, enemy));
        total = ItemStatistics.add(total, mainHandStatistics(causer, enemy));
        total = ItemStatistics.add(total, spareStatistics());
        total = ItemStatistics.add(total, getTemporaryStatistics());
        total = ItemStatistics.add(total, petStatistics());
        total = ItemStatistics.add(total, accessoryStatistics);
        total = ItemStatistics.add(total, thaumaturgyStatistics);
        total = ItemStatistics.add(total, AttributeEffectService.statistics(player, enemy));
        total = ItemStatistics.add(total, ItemStatistic.getOfAllBaseValues());

        ActivePerks perks = ElectionPerks.current();
        total = ItemStatistics.add(total, MayorPerkStatistics.flatBonuses(perks, !HypixelConst.isIslandServer()));
        total = MayorPerkStatistics.applyScaling(perks, total, enemy instanceof SlayerBossMob);

        total = net.swofty.type.skyblockgeneric.item.armor.ArmorAbilityEffects.finalise(player, total);
        return ZoneRuleService.capStatistics(player, StatOverrides.apply(player,
                net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks.withRaisedSpeedCap(player, total)));
    }

    public List<StatisticSource> statisticSources() {
        return modifierCollector.statisticSources();
    }

    public List<StatisticModifier> statisticModifiers() {
        return modifierCollector.statisticModifiers();
    }

    /**
     * the updater skips the main hand while a menu is open, which causes
     * stat menus displaying wrong stats.
     */
    SkyBlockItem getMainHandItem() {
        return new SkyBlockItem(player.getInventory().getItemStack(player.getHeldSlot()));
    }







    public record StatisticSource(String name, Material material, String texture, ItemStatistics statistics,
                                  StatisticSourceType sourceType, List<StatisticModifier> modifiers) {
    }

    private ItemStatistics equipmentStatistics(SkyBlockPlayer causer, LivingEntity enemy) {
        DatapointInventory inventory = player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class);
        List<net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem> equipment = List.of(
            inventory.getValue().getNecklace(),
            inventory.getValue().getCloak(),
            inventory.getValue().getBelt(),
            inventory.getValue().getGloves()
        );

        ItemStatistics total = ItemStatistics.empty();
        for (net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem serialized : equipment) {
            SkyBlockItem item = new SkyBlockItem(serialized);
            if (item.isAir()) continue;
            total = ItemStatistics.add(total, ItemStatistics.add(
                item.getAttributeHandler().getStatistics(),
                calculateExtraItemStatisticsToAdd(item, causer, enemy)
            ));
        }
        return total;
    }

    public void updateAccessoryStatistics() {
        AccessoryOwnership.invalidate(player);
        accessoryStatistics = AccessoryStats.of(player, AccessoryOwnership.active(player),
                item -> calculateExtraItemStatisticsToAdd(item, null, null));
        thaumaturgyStatistics = AccessoryPowers.statistics(AccessoryPowers.data(player), AccessoryPower.of(player));
        AccessoryBagXP.sync(player);
        invalidateStatistics();
    }

    private ItemStatistics calculateExtraItemStatisticsToAdd(SkyBlockItem item, SkyBlockPlayer causer, LivingEntity enemy) {
        return itemCalculator.calculateExtraItemStatisticsToAdd(item, causer, enemy);
    }


    private ItemStatistics getTemporaryStatistics() {
        ItemStatistics statistics = ItemStatistics.builder().build();

        synchronized (temporaryStatistics) {
            temporaryStatistics.removeIf(temporaryStatistic -> temporaryStatistic.getExpiration() < System.currentTimeMillis());
            for (TemporaryStatistic temporaryStatistic : temporaryStatistics) {
                statistics = ItemStatistics.add(statistics, temporaryStatistic.getStatistics());
            }

            temporaryConditionalStatistics.removeIf(temporaryStatistic -> !temporaryStatistic.getExpiry().apply(player));
            for (TemporaryConditionalStatistic temporaryStatistic : temporaryConditionalStatistics) {
                statistics = ItemStatistics.add(statistics, temporaryStatistic.getStatistics().apply(player));
            }

            return statistics;
        }
    }


    private ItemStatistics getBestiaryStatistics(SkyBlockPlayer causer, BestiaryMob enemy) {
        ItemStatistics statistics = ItemStatistics.builder().build();

        int kills = causer.getBestiaryData().getAmount(enemy);
        int tier = bestiaryData.getCurrentBestiaryTier(enemy, kills);
        double magicFind = bestiaryData.getTotalMagicFind(tier);
        double strength = bestiaryData.getTotalStrength(tier);

        if (magicFind > 0) statistics.addAdditive(ItemStatistic.MAGIC_FIND, magicFind);
        if (strength > 0) statistics.addAdditive(ItemStatistic.STRENGTH, strength);

        return statistics;
    }

    public Map.Entry<Double, Boolean> runPrimaryDamageFormula(ItemStatistics enemyStatistics, SkyBlockPlayer causer, LivingEntity enemy) {
        ItemStatistics all = allStatistics(causer, enemy);
        return runPrimaryDamageFormula(all, enemyStatistics, enemy);
    }

    public static Map.Entry<Double, Boolean> runPrimaryDamageFormula(ItemStatistics originStatistics, ItemStatistics enemyStatistics) {
        return runPrimaryDamageFormula(originStatistics, enemyStatistics, null);
    }

    private static Map.Entry<Double, Boolean> runPrimaryDamageFormula(ItemStatistics originStatistics, ItemStatistics enemyStatistics, LivingEntity enemy) {
        boolean isCrit = false;
        double critChance = originStatistics.getBase(ItemStatistic.CRITICAL_CHANCE);
        if (Math.random() <= (critChance / 100))
            isCrit = true;

        double baseDamage = originStatistics.getOverall(ItemStatistic.DAMAGE);
        double strength = originStatistics.getOverall(ItemStatistic.STRENGTH);
        double critDamage = originStatistics.getOverall(ItemStatistic.CRITICAL_DAMAGE);

        double strengthDamage = (1 + (strength / 100));
        double criticalDamage = isCrit ? 1 + (critDamage / 100) : 1;

        double damage = baseDamage * strengthDamage * criticalDamage;

        double enemyDefense = enemyStatistics.getOverall(ItemStatistic.DEFENSE);

        if (enemy instanceof SkyBlockMob) {
            double lethalityReduction = LethalityDebuff.getTotalDefenseReduction(enemy.getUuid());
            enemyDefense *= (1 - lethalityReduction);
        }

        if (enemyDefense > 0)
            damage = damage * (1 - (enemyDefense / (enemyDefense + 100)));
        return new AbstractMap.SimpleEntry<>(damage, isCrit);
    }

    public void boostManaRegeneration(double percent, int ticks) {
        manaRegenerationPercentBonus += percent;

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            manaRegenerationPercentBonus -= percent;
        }, TaskSchedule.tick(ticks), TaskSchedule.stop());
    }

    public void boostIncomingDamageReduction(double percent, int ticks) {
        incomingDamageReductionPercent += percent;

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            incomingDamageReductionPercent -= percent;
        }, TaskSchedule.tick(ticks), TaskSchedule.stop());
    }

    public void boostStatistic(TemporaryStatistic temporaryStatistic) {
        temporaryStatistics.add(temporaryStatistic);
        invalidateStatistics();
    }

    public void removeTemporaryStatistics(Predicate<TemporaryStatistic> predicate) {
        synchronized (temporaryStatistics) {
            temporaryStatistics.removeIf(predicate);
        }
        invalidateStatistics();
    }

    public void invalidateStatistics() {
        cachedAllStatistics = null;
    }

    /**
     * Get all active temporary statistics that have display info (for tablist)
     */
    public List<TemporaryStatistic> getDisplayableActiveEffects() {
        synchronized (temporaryStatistics) {
            temporaryStatistics.removeIf(stat -> stat.getExpiration() < System.currentTimeMillis());
            return temporaryStatistics.stream()
                    .filter(TemporaryStatistic::hasDisplayInfo)
                    .toList();
        }
    }

    public void boostStatistic(TemporaryConditionalStatistic temporaryStatistic) {
        temporaryConditionalStatistics.add(temporaryStatistic);
        invalidateStatistics();
    }

    public void boostHealthRegeneration(double percent, int ticks) {
        healthRegenerationPercentBonus += percent;

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            healthRegenerationPercentBonus -= percent;
        }, TaskSchedule.tick(ticks), TaskSchedule.stop());
    }

    public static void run() {
        PlayerStatisticLoops.run();
    }

    public static void experiencedStatisticsLoop() {
        PlayerStatisticLoops.experiencedStatisticsLoop();
    }

    public static void barLoop() {
        PlayerStatisticLoops.barLoop();
    }

    public static void statisticsLoop() {
        PlayerStatisticLoops.statisticsLoop();
    }

    public static void speedLoop() {
        PlayerStatisticLoops.speedLoop();
    }

    public static void healthLoop() {
        PlayerStatisticLoops.healthLoop();
    }

    public static void missionLoop() {
        PlayerStatisticLoops.missionLoop();
    }

    public static void manaLoop() {
        PlayerStatisticLoops.manaLoop();
    }
}
