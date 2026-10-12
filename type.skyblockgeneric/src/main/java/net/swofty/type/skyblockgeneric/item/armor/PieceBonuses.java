package net.swofty.type.skyblockgeneric.item.armor;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class PieceBonuses {
    public static final double ZOMBIE_HAT_DEFENSE_PER_ZOMBIE = 10;
    public static final double ZOMBIE_HAT_RANGE = 8;
    private static final Set<ServerType> MINING_ISLANDS = Set.of(ServerType.SKYBLOCK_GOLD_MINE,
            ServerType.SKYBLOCK_DEEP_CAVERNS, ServerType.SKYBLOCK_DWARVEN_MINES);
    private static final Set<String> TANK_MINER = Set.of("TANK_MINER_HELMET", "TANK_MINER_CHESTPLATE",
            "TANK_MINER_LEGGINGS", "TANK_MINER_BOOTS");
    private static final Set<String> NUTCRACKER = Set.of("NUTCRACKER_HELMET", "NUTCRACKER_CHESTPLATE",
            "NUTCRACKER_LEGGINGS", "NUTCRACKER_BOOTS");

    private PieceBonuses() {
    }

    public static double depthCoating(double pieceDefense, boolean onMiningIsland) {
        return onMiningIsland ? Math.max(0, pieceDefense) : 0;
    }

    public static double zombieHatDefense(int zombiesNearby) {
        return Math.max(0, zombiesNearby) * ZOMBIE_HAT_DEFENSE_PER_ZOMBIE;
    }

    public static boolean isOnMiningIsland(@Nullable ServerType server) {
        return server != null && MINING_ISLANDS.contains(server);
    }

    public static ItemStatistics extraItemStatistics(@Nullable SkyBlockItem item, @Nullable SkyBlockPlayer wearer) {
        if (wearer == null || item == null || item.isAir()) return ItemStatistics.empty();
        String id = item.getAttributeHandler().getTypeAsString();
        if (id == null) return ItemStatistics.empty();
        ServerType server = HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType();
        if (TANK_MINER.contains(id)) {
            double bonus = depthCoating(item.getAttributeHandler().getStatistics().getOverall(ItemStatistic.DEFENSE),
                    isOnMiningIsland(server));
            return bonus == 0 ? ItemStatistics.empty() : ItemStatistics.builder().withBase(ItemStatistic.DEFENSE, bonus).build();
        }
        if (NUTCRACKER.contains(id)) {
            return server == ServerType.SKYBLOCK_JERRYS_WORKSHOP ? item.getAttributeHandler().getStatistics() : ItemStatistics.empty();
        }
        if ("ZOMBIE_HAT".equals(id)) {
            double bonus = zombieHatDefense(zombiesNear(wearer));
            return bonus == 0 ? ItemStatistics.empty() : ItemStatistics.builder().withBase(ItemStatistic.DEFENSE, bonus).build();
        }
        return ItemStatistics.empty();
    }

    private static int zombiesNear(SkyBlockPlayer player) {
        if (player.getInstance() == null) return 0;
        int count = 0;
        for (Entity entity : player.getInstance().getNearbyEntities(player.getPosition(), ZOMBIE_HAT_RANGE)) {
            if (entity instanceof SkyBlockMob mob && mob.getEntityType() == EntityType.ZOMBIE) count++;
        }
        return count;
    }
}
