package dev.daweapon.type.skyblockgeneric.fishing.lava;

import net.minestom.server.entity.LivingEntity;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lava fishing gear bonuses (wiki: Lava_Sea_Creature_Armor, Thunder_Armor, Magma_Lord_Armor, Bronze_Hunter_Armor…):
 * per-piece Magmatic damage reduction and multiplier, Hunter armor's Odger's Blessing Trophy Chance, Magma Lord's
 * Lord's Blessing, and Thunder's Static Charge. Fireproof (lava immunity) and Star Fisher (needs star upgrades) are
 * not modelled. Reductions and multipliers stack multiplicatively per piece (estimate).
 */
public final class CrimsonFishingGear {
    private static final Map<String, double[]> MAGMATIC = Map.ofEntries(
            Map.entry("TAURUS_HELMET", new double[]{0.95, 1.1}), Map.entry("FLAMING_CHESTPLATE", new double[]{0.95, 1.1}),
            Map.entry("MOOGMA_LEGGINGS", new double[]{0.95, 1.1}), Map.entry("SLUG_BOOTS", new double[]{0.95, 1.1}),
            Map.entry("THUNDER_HELMET", new double[]{0.9, 1.2}), Map.entry("THUNDER_CHESTPLATE", new double[]{0.9, 1.2}),
            Map.entry("THUNDER_LEGGINGS", new double[]{0.9, 1.2}), Map.entry("THUNDER_BOOTS", new double[]{0.9, 1.2}),
            Map.entry("THUNDERBOLT_NECKLACE", new double[]{0.95, 1.1}),
            Map.entry("MAGMA_LORD_HELMET", new double[]{0.85, 1.3}), Map.entry("MAGMA_LORD_CHESTPLATE", new double[]{0.85, 1.3}),
            Map.entry("MAGMA_LORD_LEGGINGS", new double[]{0.85, 1.3}), Map.entry("MAGMA_LORD_BOOTS", new double[]{0.85, 1.3}));
    private static final Map<String, double[]> ODGERS_BLESSING = Map.of(
            "BRONZE", new double[]{0, 3, 4, 5}, "SILVER", new double[]{0, 5, 7.5, 10},
            "GOLD", new double[]{0, 10, 15, 20}, "DIAMOND", new double[]{0, 20, 25, 30});
    private static final Set<String> THUNDER = Set.of("THUNDER_HELMET", "THUNDER_CHESTPLATE", "THUNDER_LEGGINGS", "THUNDER_BOOTS", "THUNDERBOLT_NECKLACE");
    private static final Set<String> MAGMA_LORD = Set.of("MAGMA_LORD_HELMET", "MAGMA_LORD_CHESTPLATE", "MAGMA_LORD_LEGGINGS", "MAGMA_LORD_BOOTS", "MAGMA_LORD_NECKLACE");
    private static final int[] CHARGE_SECONDS = {0, 0, 30, 25, 20, 10};
    private static final double[] CHARGE_DAMAGE = {0, 0, 0.15, 0.20, 0.25, 0.40};
    private static final Map<UUID, Charge> CHARGES = new ConcurrentHashMap<>();

    private CrimsonFishingGear() {
    }

    static List<String> worn(SkyBlockPlayer player) {
        List<String> ids = new ArrayList<>();
        for (SkyBlockItem piece : player.getArmor()) add(ids, piece);
        if (player.getSkyblockDataHandler() != null) {
            var necklace = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class)
                    .getValue().getNecklace();
            if (necklace != null) add(ids, new SkyBlockItem(necklace));
        }
        return ids;
    }

    private static void add(List<String> ids, SkyBlockItem item) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        if (type != null) ids.add(type.name());
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        List<String> worn = worn(player);
        double trophyChance = 0;
        for (Map.Entry<String, double[]> tier : ODGERS_BLESSING.entrySet()) {
            int pieces = (int) worn.stream().filter(id -> id.startsWith(tier.getKey() + "_HUNTER_")).count();
            if (pieces > 0) trophyChance = Math.max(trophyChance, tier.getValue()[pieces - 1]);
        }
        int lordPieces = count(worn, MAGMA_LORD);
        double seaCreatureChance = lordPieces >= 5 ? 1.5 : lordPieces >= 4 ? 1 : 0;
        if (trophyChance == 0 && seaCreatureChance == 0) return ItemStatistics.empty();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.TROPHY_FISH_CHANCE, trophyChance)
                .withBase(ItemStatistic.SEA_CREATURE_CHANCE, seaCreatureChance)
                .build();
    }

    public static ItemStatistics against(SkyBlockPlayer player, @Nullable LivingEntity enemy) {
        if (!magmatic(enemy)) return ItemStatistics.empty();
        int lordPieces = count(worn(player), MAGMA_LORD);
        double magicFind = lordPieces >= 5 ? 30 : lordPieces >= 4 ? 20 : 0;
        if (magicFind == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.MAGIC_FIND, magicFind).build();
    }

    public static double incomingMultiplier(SkyBlockPlayer player, @Nullable LivingEntity attacker) {
        if (!magmatic(attacker)) return 1;
        double multiplier = 1;
        for (String id : worn(player)) {
            double[] values = MAGMATIC.get(id);
            if (values != null) multiplier *= values[0];
        }
        return multiplier;
    }

    /** Outgoing melee multiplier: per-piece Magmatic bonus, then any Static Charge discharged by this hit. */
    public static double outgoingMultiplier(SkyBlockPlayer player, @Nullable LivingEntity target) {
        List<String> worn = worn(player);
        double multiplier = 1;
        if (magmatic(target)) {
            for (String id : worn) {
                double[] values = MAGMATIC.get(id);
                if (values != null) multiplier *= values[1];
            }
        }
        return multiplier * discharge(player, count(worn, THUNDER));
    }

    static int maxCharges(int pieces) {
        return pieces >= 2 ? pieces : 0;
    }

    private static double discharge(SkyBlockPlayer player, int thunderPieces) {
        int max = maxCharges(thunderPieces);
        if (max == 0) {
            CHARGES.remove(player.getUuid());
            return 1;
        }
        long now = System.currentTimeMillis();
        Charge charge = CHARGES.computeIfAbsent(player.getUuid(), _ -> new Charge(now));
        int gained = (int) ((now - charge.since) / (CHARGE_SECONDS[thunderPieces] * 1000L));
        int charges = Math.min(max, gained);
        charge.since = now;
        return 1 + charges * CHARGE_DAMAGE[thunderPieces];
    }

    private static int count(List<String> worn, Set<String> set) {
        return (int) worn.stream().filter(set::contains).count();
    }

    private static boolean magmatic(@Nullable LivingEntity entity) {
        return entity instanceof SkyBlockMob mob && mob.getMobTypes().contains(MobType.MAGMATIC);
    }

    private static final class Charge {
        long since;

        Charge(long since) {
            this.since = since;
        }
    }
}
