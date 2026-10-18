package dev.daweapon.type.skyblockgeneric.item.equipment.passive;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMagmaCube;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobPackMagmaCube;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class EquipmentPassives {
    private static final Map<UUID, Long> NEXT_MELEE = new ConcurrentHashMap<>();

    public enum Counter {
        BLAZE_CONSUMER(EquipmentPassiveMath.VANQUISHED_BLAZE_BELT, "equipment:blaze_kills", ItemStatistic.STRENGTH,
                "Blaze Consumer", "<c>❁ Strength"),
        MAGMA_CUBE_ABSORBER(EquipmentPassiveMath.VANQUISHED_MAGMA_NECKLACE, "equipment:magma_cube_kills",
                ItemStatistic.CRITICAL_DAMAGE, "Magma Cube Absorber", "<9>☠ Crit Damage"),
        GHAST_BLASTER(EquipmentPassiveMath.VANQUISHED_GHAST_CLOAK, "equipment:ghast_kills", ItemStatistic.DEFENSE,
                "Ghast Blaster", "<a>❈ Defense"),
        GLOWING(EquipmentPassiveMath.VANQUISHED_GLOWSTONE_GAUNTLET, "equipment:glowstone_mined",
                ItemStatistic.INTELLIGENCE, "Glowing", "<b>✎ Intelligence");

        private final String itemId;
        private final String key;
        private final ItemStatistic statistic;
        private final String bonusName;
        private final String statisticDisplay;

        Counter(String itemId, String key, ItemStatistic statistic, String bonusName, String statisticDisplay) {
            this.itemId = itemId;
            this.key = key;
            this.statistic = statistic;
            this.bonusName = bonusName;
            this.statisticDisplay = statisticDisplay;
        }
    }

    private EquipmentPassives() {
    }

    public static Set<String> worn(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return Set.of();
        var inventory = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue();
        Set<String> ids = new HashSet<>();
        for (var serialized : Arrays.asList(inventory.getNecklace(), inventory.getCloak(), inventory.getBelt(), inventory.getGloves())) {
            if (serialized == null) continue;
            SkyBlockItem item = new SkyBlockItem(serialized);
            if (item.isNA() || item.isAir()) continue;
            String id = item.getAttributeHandler().getTypeAsString();
            if (id != null) ids.add(id);
        }
        return ids;
    }

    public static boolean wearing(SkyBlockPlayer player, String itemId) {
        return worn(player).contains(itemId);
    }

    public static long count(SkyBlockPlayer player, Counter counter) {
        return DatapointCrimsonIsle.of(player).getNumber(counter.key);
    }

    public static void increment(SkyBlockPlayer player, Counter counter) {
        if (!wearing(player, counter.itemId)) return;
        long before = count(player, counter);
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().merge(counter.key, 1L, Long::sum));
        int bonusBefore = EquipmentPassiveMath.counterBonus(before);
        int bonusAfter = EquipmentPassiveMath.counterBonus(before + 1);
        if (bonusAfter == bonusBefore) return;
        player.getStatistics().invalidateStatistics();
        player.sendMessage("<6>" + counter.bonusName + " <7>upgraded: <r>" + counter.statisticDisplay + " <7>bonus is now <a>+" + bonusAfter + "<7>.");
    }

    public static void onKill(SkyBlockPlayer player, SkyBlockMob mob) {
        if (mob.getEntityType().equals(EntityType.BLAZE)) increment(player, Counter.BLAZE_CONSUMER);
        else if (mob.getEntityType().equals(EntityType.GHAST)) increment(player, Counter.GHAST_BLASTER);
        else if (isMagmaCube(mob)) increment(player, Counter.MAGMA_CUBE_ABSORBER);
    }

    public static boolean isMagmaCube(SkyBlockMob mob) {
        return mob instanceof MobMagmaCube || mob instanceof MobPackMagmaCube;
    }

    public static ItemStatistics finalise(SkyBlockPlayer player, ItemStatistics total) {
        Set<String> worn = worn(player);
        if (worn.isEmpty()) return total;
        ItemStatistics.Builder bonus = ItemStatistics.builder();
        boolean any = false;
        for (Counter counter : Counter.values()) {
            if (!worn.contains(counter.itemId)) continue;
            int amount = EquipmentPassiveMath.counterBonus(count(player, counter));
            if (amount <= 0) continue;
            bonus.withBase(counter.statistic, (double) amount);
            any = true;
        }
        if (worn.contains(EquipmentPassiveMath.SCOVILLE_BELT)) {
            double defense = EquipmentPassiveMath.scovilleDefense(ignitedEnemiesNear(player));
            if (defense > 0) {
                bonus.withBase(ItemStatistic.DEFENSE, defense);
                any = true;
            }
        }
        if (any) total = ItemStatistics.add(total, bonus.build());
        if (worn.contains(EquipmentPassiveMath.LAVA_SHELL_NECKLACE)) {
            total = total.withExactValue(ItemStatistic.HEALTH_REGENERATION, 0).withExactValue(ItemStatistic.MENDING, 0);
        }
        return total;
    }

    public static int ignitedEnemiesNear(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return 0;
        int ignited = 0;
        for (Entity entity : instance.getNearbyEntities(player.getPosition(), EquipmentPassiveMath.SCOVILLE_RADIUS)) {
            if (entity instanceof SkyBlockMob mob && !mob.isDead() && !mob.isRemoved() && mob.getFireTicks() > 0
                    && mob.getPosition().distance(player.getPosition()) <= EquipmentPassiveMath.SCOVILLE_RADIUS) ignited++;
        }
        return ignited;
    }

    public static double explosionMultiplier(SkyBlockPlayer player) {
        return wearing(player, EquipmentPassiveMath.IMPLOSION_BELT) ? EquipmentPassiveMath.IMPLOSION_BELT_MULTIPLIER : 1;
    }

    public static int magmaCream(SkyBlockPlayer player, SkyBlockMob mob, ItemType type, int amount) {
        if (!type.name().equals("MAGMA_CREAM") || !isMagmaCube(mob)) return amount;
        double percent = EquipmentPassiveMath.magmaCreamPercent(worn(player));
        return EquipmentPassiveMath.scaledAmount(amount, percent, ThreadLocalRandom.current());
    }

    public static boolean meleeReady(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        if (now < NEXT_MELEE.getOrDefault(player.getUuid(), 0L)) return false;
        NEXT_MELEE.put(player.getUuid(), now + player.getStatistics().getInvulnerabilityTime() * 50L);
        return true;
    }
}
