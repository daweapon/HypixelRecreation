package dev.daweapon.type.skyblockgeneric.item.armor;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobStatusEffects;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockInventory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class ArmorAbilityEffects {
    public static final Set<String> HEAT = Set.of("HEAT_HELMET", "HEAT_CHESTPLATE", "HEAT_LEGGINGS", "HEAT_BOOTS");
    public static final Set<String> BAT_PERSON = Set.of("BAT_PERSON_HELMET", "BAT_PERSON_CHESTPLATE",
            "BAT_PERSON_LEGGINGS", "BAT_PERSON_BOOTS");
    public static final Set<String> PACK = Set.of("HELMET_OF_THE_PACK", "CHESTPLATE_OF_THE_PACK",
            "LEGGINGS_OF_THE_PACK");
    public static final Set<String> MASTIFF = Set.of("MASTIFF_HELMET", "MASTIFF_CHESTPLATE",
            "MASTIFF_LEGGINGS", "MASTIFF_BOOTS");
    public static final Set<String> NUTCRACKER = Set.of("NUTCRACKER_HELMET", "NUTCRACKER_CHESTPLATE",
            "NUTCRACKER_LEGGINGS", "NUTCRACKER_BOOTS");
    public static final Set<String> WEREWOLF = Set.of("WEREWOLF_HELMET", "WEREWOLF_CHESTPLATE",
            "WEREWOLF_LEGGINGS", "WEREWOLF_BOOTS");
    public static final Set<String> FINAL_DESTINATION = Set.of("FINAL_DESTINATION_HELMET",
            "FINAL_DESTINATION_CHESTPLATE", "FINAL_DESTINATION_LEGGINGS", "FINAL_DESTINATION_BOOTS");
    public static final Set<String> TARANTULA = Set.of("TARANTULA_HELMET", "TARANTULA_CHESTPLATE",
            "TARANTULA_LEGGINGS", "TARANTULA_BOOTS");
    public static final Set<String> EMBER = Set.of("EMBER_HELMET", "EMBER_CHESTPLATE", "EMBER_LEGGINGS", "EMBER_BOOTS");
    public static final Set<String> ARACHNE = Set.of("ARACHNE_HELMET", "ARACHNE_CHESTPLATE", "ARACHNE_LEGGINGS",
            "ARACHNE_BOOTS", "ARACHNE_NECKLACE", "ARACHNE_CLOAK", "ARACHNE_BELT", "ARACHNE_GLOVES");
    public static final double AURA_RANGE = 30;
    public static final double HOWL_HEAL_RANGE = 25;
    public static final double HOWL_HEAL_DEFENSE_PERCENT = 1;
    private static final Set<ItemStatistic> COMBAT_STATISTICS = Set.of(ItemStatistic.HEALTH, ItemStatistic.DEFENSE,
            ItemStatistic.STRENGTH, ItemStatistic.INTELLIGENCE, ItemStatistic.CRITICAL_CHANCE,
            ItemStatistic.CRITICAL_DAMAGE, ItemStatistic.BONUS_ATTACK_SPEED, ItemStatistic.ABILITY_DAMAGE,
            ItemStatistic.TRUE_DEFENSE, ItemStatistic.FEROCITY, ItemStatistic.HEALTH_REGENERATION,
            ItemStatistic.VITALITY, ItemStatistic.MENDING);

    private ArmorAbilityEffects() {
    }

    public static ItemStatistics pieceStatistics(@Nullable SkyBlockItem item, @Nullable SkyBlockPlayer wearer) {
        if (wearer == null || item == null || item.isAir()) return ItemStatistics.empty();
        String id = item.getAttributeHandler().getTypeAsString();
        if (id == null) return ItemStatistics.empty();
        if (HEAT.contains(id)) return pressurizedCoating(item);
        if (BAT_PERSON.contains(id)) return batPerson(item, wearer);
        if (PACK.contains(id)) {
            return ItemStatistics.builder().withBase(ItemStatistic.TRUE_DEFENSE, ArmorAbilityMath.PACK_TRUE_DEFENSE).build();
        }
        if ("OBSIDIAN_CHESTPLATE".equals(id)) {
            double speed = ArmorAbilityMath.obsidianSpeed(obsidianCount(wearer));
            return speed == 0 ? ItemStatistics.empty() : ItemStatistics.builder().withBase(ItemStatistic.SPEED, speed).build();
        }
        return ItemStatistics.empty();
    }

    public static ItemStatistics setStatistics(SkyBlockPlayer player) {
        List<String> worn = WornArmor.ids(player);
        ItemStatistics total = ItemStatistics.empty();
        long now = System.currentTimeMillis();
        int battalion = ArmorAbilityMath.battalionStacks(nearbyWearers(player, NUTCRACKER, true));
        if (battalion > 0) {
            total = ItemStatistics.add(total, ItemStatistics.builder()
                    .withBase(ItemStatistic.MAGIC_FIND, battalion * ArmorAbilityMath.BATTALION_MAGIC_FIND)
                    .withBase(ItemStatistic.DEFENSE, battalion * ArmorAbilityMath.BATTALION_DEFENSE).build());
        }
        double faithful = ArmorAbilityMath.arachnesFaithful(arachnePieces(player, worn));
        if (faithful > 0) {
            total = ItemStatistics.add(total, ItemStatistics.builder()
                    .withBase(ItemStatistic.HEALTH, faithful).withBase(ItemStatistic.DEFENSE, faithful).build());
        }
        if (WornArmor.wearsAll(worn, WEREWOLF)) {
            int stacks = ArmorAbilityState.howlStacks(player, now);
            if (stacks > 0) {
                total = ItemStatistics.add(total, ItemStatistics.builder()
                        .withBase(ItemStatistic.DEFENSE, stacks * ArmorAbilityMath.HOWL_DEFENSE_PER_STACK).build());
            }
        }
        if (WornArmor.wearsAll(worn, FINAL_DESTINATION) && ArmorAbilityState.darknessActive(player, now)) {
            total = ItemStatistics.add(total, ItemStatistics.builder()
                    .withBase(ItemStatistic.STRENGTH, ArmorAbilityMath.DARKNESS_STRENGTH)
                    .withBase(ItemStatistic.BONUS_ATTACK_SPEED, ArmorAbilityMath.DARKNESS_ATTACK_SPEED)
                    .withBase(ItemStatistic.SPEED, ArmorAbilityMath.DARKNESS_SPEED)
                    .withMultiplicative(ItemStatistic.INTELLIGENCE, ArmorAbilityMath.DARKNESS_INTELLIGENCE_MULTIPLIER)
                    .build());
        }
        return total;
    }

    public static ItemStatistics againstStatistics(@Nullable SkyBlockPlayer player, @Nullable LivingEntity enemy) {
        if (player == null || !(enemy instanceof SkyBlockMob mob) || !mob.getMobTypes().contains(MobType.ENDER)) {
            return ItemStatistics.empty();
        }
        if (!darknessActiveNow(player)) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.FEROCITY, ArmorAbilityMath.DARKNESS_ENDERMAN_FEROCITY).build();
    }

    public static void afterMeleeHit(SkyBlockPlayer player, SkyBlockMob mob, float damage, boolean critical) {
        if (!WornArmor.wearsAll(WornArmor.cachedIds(player), TARANTULA)) return;
        if (!ArmorAbilityMath.isOctodexterityStrike(ArmorAbilityState.nextStrike(player))) return;
        AttackService.applyHit(player, mob, (float) (damage * ArmorAbilityMath.OCTODEXTERITY_DAMAGE_BONUS / 100D), critical);
        MobStatusEffects.reduceHealing(mob, ArmorAbilityMath.VENOM_HEALING_REDUCTION, ArmorAbilityMath.VENOM_MILLIS);
    }

    public static void onFerocityActivated(SkyBlockPlayer player) {
        if (WornArmor.wearsAll(WornArmor.cachedIds(player), WEREWOLF)) {
            ArmorAbilityState.recordHowl(player, System.currentTimeMillis());
            healNearby(player);
        }
    }

    public static boolean darknessActiveNow(SkyBlockPlayer player) {
        return WornArmor.wearsAll(WornArmor.ids(player), FINAL_DESTINATION)
                && ArmorAbilityState.darknessActive(player, System.currentTimeMillis());
    }

    public static ItemStatistics finalise(SkyBlockPlayer player, ItemStatistics total) {
        List<String> worn = WornArmor.ids(player);
        ItemStatistics result = total;
        if (WornArmor.wearsAll(worn, MASTIFF)) result = absoluteUnit(result);
        if ("TARANTULA_HELMET".equals(worn.getFirst())) {
            result = ItemStatistics.add(result, ItemStatistics.builder().withBase(ItemStatistic.CRITICAL_DAMAGE,
                    ArmorAbilityMath.radioactiveCritDamage(result.getOverall(ItemStatistic.STRENGTH))).build());
        }
        if (worn.contains("CLOVER_HELMET")) result = cloverPenalty(result);
        if (worn.contains("RACING_HELMET")) result = racingSpeedCap(result);
        return result;
    }

    public static ItemStatistics hotspotBuffs(SkyBlockPlayer player) {
        if (!"TIKI_MASK".equals(WornArmor.id(player.getHelmet()))) return ItemStatistics.empty();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.FISHING_SPEED, ArmorAbilityMath.TIKI_FISHING_SPEED)
                .withBase(ItemStatistic.SEA_CREATURE_CHANCE, ArmorAbilityMath.TIKI_SEA_CREATURE_CHANCE)
                .withBase(ItemStatistic.DOUBLE_HOOK_CHANCE, ArmorAbilityMath.TIKI_DOUBLE_HOOK_CHANCE)
                .withBase(ItemStatistic.TREASURE_CHANCE, ArmorAbilityMath.TIKI_TREASURE_CHANCE)
                .build();
    }

    public static ItemStatistics absoluteUnit(ItemStatistics total) {
        double defense = Math.min(ArmorAbilityMath.MASTIFF_DEFENSE_CAP, total.getOverall(ItemStatistic.DEFENSE));
        ItemStatistics result = total.withExactValue(ItemStatistic.DEFENSE, defense);
        result = ItemStatistics.add(result, ItemStatistics.builder()
                .withBase(ItemStatistic.CRITICAL_DAMAGE, ArmorAbilityMath.mastiffCritDamage(defense)).build());
        double healthPercent = ArmorAbilityMath.mastiffHealthPercent(result.getOverall(ItemStatistic.CRITICAL_DAMAGE));
        return healthPercent == 0 ? result : ItemStatistics.add(result,
                ItemStatistics.builder().withAdditivePercentage(ItemStatistic.HEALTH, healthPercent).build());
    }

    public static ItemStatistics cloverPenalty(ItemStatistics total) {
        ItemStatistics result = total;
        double factor = 1 + ArmorAbilityMath.CLOVER_OTHER_STATS_PERCENT / 100D;
        for (ItemStatistic stat : ItemStatistic.values()) {
            if (stat == ItemStatistic.MAGIC_FIND) continue;
            double value = total.getOverall(stat);
            if (value != 0) result = result.withExactValue(stat, value * factor);
        }
        return result;
    }

    public static ItemStatistics racingSpeedCap(ItemStatistics total) {
        double raw = total.getBase(ItemStatistic.SPEED) * total.getAdditive(ItemStatistic.SPEED)
                * total.getMultiplicative(ItemStatistic.SPEED);
        double cap = ItemStatistic.SPEED.getCap() == null ? Double.MAX_VALUE : ItemStatistic.SPEED.getCap();
        return total.withExactValue(ItemStatistic.SPEED,
                ArmorAbilityMath.raisedSpeedCap(raw, cap, ArmorAbilityMath.RACING_SPEED_CAP_BONUS));
    }

    private static void healNearby(SkyBlockPlayer player) {
        if (player.getInstance() == null) return;
        float heal = (float) (player.getStatistics().allStatistics().getOverall(ItemStatistic.DEFENSE)
                * HOWL_HEAL_DEFENSE_PERCENT / 100D);
        for (SkyBlockPlayer other : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (other == player || other.getInstance() != player.getInstance() || other.isDead()) continue;
            if (other.getPosition().distance(player.getPosition()) > HOWL_HEAL_RANGE) continue;
            other.setHealth(Math.min(other.getMaxHealth(), other.getHealth() + heal));
        }
    }

    private static ItemStatistics pressurizedCoating(SkyBlockItem item) {
        ServerType server = HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType();
        if (!PieceBonuses.isOnMiningIsland(server)) return ItemStatistics.empty();
        ItemStatistics own = item.getAttributeHandler().getStatistics();
        double extra = ArmorAbilityMath.HEAT_COATING_MULTIPLIER - 1;
        return ItemStatistics.builder()
                .withBase(ItemStatistic.DEFENSE, own.getOverall(ItemStatistic.DEFENSE) * extra)
                .withBase(ItemStatistic.STRENGTH, own.getOverall(ItemStatistic.STRENGTH) * extra)
                .build();
    }

    private static ItemStatistics batPerson(SkyBlockItem item, SkyBlockPlayer wearer) {
        boolean festival = SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.SPOOKY_FESTIVAL);
        boolean night = wearer.getInstance() != null && ArmorAbilityMath.isNight(wearer.getInstance().getTime());
        double extra = ArmorAbilityMath.batPersonMultiplier(festival, night) - 1;
        if (extra == 0) return ItemStatistics.empty();
        ItemStatistics own = item.getAttributeHandler().getStatistics();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic stat : COMBAT_STATISTICS) {
            double value = own.getOverall(stat);
            if (value != 0) builder.withBase(stat, value * extra);
        }
        return builder.build();
    }

    public static int nutcrackerPieces(SkyBlockPlayer player) {
        return WornArmor.count(WornArmor.cachedIds(player), NUTCRACKER);
    }

    private static int arachnePieces(SkyBlockPlayer player, List<String> worn) {
        int count = 0;
        for (String id : worn) if (ARACHNE.contains(id)) count++;
        if (player.getSkyblockDataHandler() == null) return count;
        SkyBlockInventory inventory = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue();
        for (UnderstandableSkyBlockItem equipment : Arrays.asList(inventory.getNecklace(), inventory.getCloak(),
                inventory.getBelt(), inventory.getGloves())) {
            if (equipment == null) continue;
            SkyBlockItem item = new SkyBlockItem(equipment);
            if (!item.isAir() && ARACHNE.contains(item.getAttributeHandler().getTypeAsString())) count++;
        }
        return count;
    }

    private static int obsidianCount(SkyBlockPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getItemStacks()) {
            if ("OBSIDIAN".equals(WornArmor.id(stack))) count += stack.amount();
        }
        return count;
    }

    private static int nearbyWearers(SkyBlockPlayer player, Set<String> pieces, boolean includeSelf) {
        if (player.getInstance() == null) return 0;
        int count = 0;
        for (SkyBlockPlayer other : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (other == player ? !includeSelf : other.getInstance() != player.getInstance()) continue;
            if (other != player && other.getPosition().distance(player.getPosition()) > AURA_RANGE) continue;
            if (WornArmor.wearsAll(WornArmor.cachedIds(other), pieces)) count++;
        }
        return count;
    }
}
