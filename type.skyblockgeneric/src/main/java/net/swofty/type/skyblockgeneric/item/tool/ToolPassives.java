package net.swofty.type.skyblockgeneric.item.tool;

import net.minestom.server.entity.Entity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.hotm.HotmPowder;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public final class ToolPassives {
    public static final int BINGOLIBUR_MAX_MAGIC_FIND = 50;
    public static final double ZOMBIE_FORTUNE_PER_UNDEAD = 5;
    public static final double ZOMBIE_FORTUNE_CAP = 25;
    public static final double ZOMBIE_RANGE = 30;
    public static final double LAPIS_DOUBLE_XP_CHANCE = 25;
    public static final double GEMSTONE_DRILL_MINING_SPEED = 800;
    public static final double STONK_EXTRA_END_STONE_CHANCE = 5;

    private static final Map<String, Double> PARK_FORAGING_FORTUNE = Map.of("JUNGLE_AXE", 50.0, "TREECAPITATOR_AXE", 100.0);
    private static final Set<RegionType> THE_PARK = Set.of(RegionType.BIRCH_PARK, RegionType.SPRUCE_WOODS,
            RegionType.DARK_THICKET, RegionType.SAVANNA_WOODLAND, RegionType.JUNGLE_ISLAND);
    private static final Map<String, Double> MITHRIL_POWDER = Map.of("MITHRIL_DRILL_1", 20.0, "MITHRIL_DRILL_2", 40.0);
    private static final Map<String, Double> GEMSTONE_POWDER = Map.of("GEMSTONE_DRILL_1", 5.0, "GEMSTONE_DRILL_2", 10.0,
            "GEMSTONE_DRILL_3", 15.0, "GEMSTONE_DRILL_4", 20.0);

    private ToolPassives() {
    }

    public static double bingoliburMagicFind(int skyBlockLevel) {
        return Math.max(0, Math.min(BINGOLIBUR_MAX_MAGIC_FIND, skyBlockLevel));
    }

    public static double zombieMiningFortune(int undeadNearby) {
        return Math.min(ZOMBIE_FORTUNE_CAP, Math.max(0, undeadNearby) * ZOMBIE_FORTUNE_PER_UNDEAD);
    }

    public static double parkForagingFortune(String itemId, boolean inPark) {
        return inPark ? PARK_FORAGING_FORTUNE.getOrDefault(itemId, 0.0) : 0;
    }

    public static boolean isInThePark(@Nullable RegionType region) {
        return region != null && THE_PARK.contains(region);
    }

    public static double powderPercent(String itemId, HotmPowder powder) {
        return switch (powder) {
            case MITHRIL -> MITHRIL_POWDER.getOrDefault(itemId, 0.0);
            case GEMSTONE -> GEMSTONE_POWDER.getOrDefault(itemId, 0.0);
            default -> 0.0;
        };
    }

    public static boolean isGemstoneDrill(String itemId) {
        return GEMSTONE_POWDER.containsKey(itemId);
    }

    public static ItemStatistics extraItemStatistics(SkyBlockItem item, @Nullable SkyBlockPlayer wearer) {
        if (wearer == null || item == null || item.isAir()) return ItemStatistics.empty();
        String id = item.getAttributeHandler().getTypeAsString();
        if (id == null) return ItemStatistics.empty();
        return switch (id) {
            case "BINGOLIBUR" -> single(ItemStatistic.MAGIC_FIND,
                    bingoliburMagicFind(wearer.getSkyBlockExperience().getLevel().asInt()));
            case "ZOMBIE_PICKAXE" -> single(ItemStatistic.MINING_FORTUNE, zombieMiningFortune(undeadNearby(wearer)));
            case "JUNGLE_AXE", "TREECAPITATOR_AXE" -> {
                SkyBlockRegion region = wearer.getRegion();
                yield single(ItemStatistic.FORAGING_FORTUNE,
                        parkForagingFortune(id, isInThePark(region == null ? null : region.getType())));
            }
            default -> ItemStatistics.empty();
        };
    }

    public static double miningSpeedBonus(SkyBlockItem tool, boolean gemstoneBlock) {
        if (!gemstoneBlock || tool == null || tool.isAir()) return 0;
        return isGemstoneDrill(tool.getAttributeHandler().getTypeAsString()) ? GEMSTONE_DRILL_MINING_SPEED : 0;
    }

    public static double heldPowderPercent(SkyBlockPlayer player, HotmPowder powder) {
        SkyBlockItem held = new SkyBlockItem(player.getInventory().getItemStack(player.getHeldSlot()));
        if (held.isAir()) return 0;
        return powderPercent(held.getAttributeHandler().getTypeAsString(), powder);
    }

    public static boolean holding(SkyBlockPlayer player, String itemId) {
        SkyBlockItem held = new SkyBlockItem(player.getInventory().getItemStack(player.getHeldSlot()));
        return !held.isAir() && itemId.equals(held.getAttributeHandler().getTypeAsString());
    }

    private static int undeadNearby(SkyBlockPlayer player) {
        if (player.getInstance() == null) return 0;
        int count = 0;
        for (Entity entity : player.getInstance().getNearbyEntities(player.getPosition(), ZOMBIE_RANGE)) {
            if (entity instanceof SkyBlockMob mob && mob.getMobTypes().contains(MobType.UNDEAD)) count++;
        }
        return count;
    }

    private static ItemStatistics single(ItemStatistic statistic, double value) {
        return value == 0 ? ItemStatistics.empty() : ItemStatistics.builder().withBase(statistic, value).build();
    }
}
