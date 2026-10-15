package dev.daweapon.type.skyblockgeneric.item.equipment;

import net.minestom.server.entity.LivingEntity;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContest;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EquipmentBonuses {
    public static final Set<String> MITHRIL_SET = Set.of("MITHRIL_NECKLACE", "MITHRIL_CLOAK", "MITHRIL_BELT", "MITHRIL_GAUNTLET");
    public static final Set<String> TITANIUM_SET = Set.of("TITANIUM_NECKLACE", "TITANIUM_CLOAK", "TITANIUM_BELT", "TITANIUM_GAUNTLET");
    public static final double MITHRIL_BLOCK_FORTUNE = 5;
    public static final double TITANIUM_BLOCK_FORTUNE = 10;
    public static final double DWARF_WANNABE_SPEED = 30;
    public static final double DWARF_WANNABE_FORTUNE = 5;
    public static final double TRUE_DWARF_SPEED = 50;
    public static final double TRUE_DWARF_FORTUNE = 40;
    public static final double KNUCKLE_SANDWICH_FIST_MULTIPLIER = 6;
    public static final double SHRIVELED_COMBAT_WISDOM = 5;
    public static final double SHRIVELED_MAGIC_FIND = 10;
    public static final double PELT_BELT_SPEED = 30;
    public static final double DRAGONFUSE_DAMAGE = 35;
    public static final double DRAGONFUSE_STRENGTH = 50;
    public static final String ASPECT_OF_THE_DRAGONS = "ASPECT_OF_THE_DRAGON";

    private static final Map<String, Integer> END_MULTIPLIERS = Map.of(
            "ENDER_NECKLACE", 2, "ENDER_CLOAK", 2, "ENDER_BELT", 2, "ENDER_GAUNTLET", 2, "DRAGONFADE_CLOAK", 3);

    private EquipmentBonuses() {
    }

    public record Situation(@Nullable ServerType server, @Nullable RegionType region, boolean contestActive,
                            boolean arthropodEnemy, @Nullable String heldItemId) {
    }

    public static ItemStatistics statistics(Map<String, ItemStatistics> worn, Situation situation) {
        ItemStatistics.Builder total = ItemStatistics.builder();
        if (situation.server() == ServerType.SKYBLOCK_DWARVEN_MINES || situation.server() == ServerType.SKYBLOCK_CRYSTAL_HOLLOWS) {
            if (worn.keySet().containsAll(MITHRIL_SET)) {
                total.withBase(ItemStatistic.MINING_SPEED, DWARF_WANNABE_SPEED);
                total.withBase(ItemStatistic.MINING_FORTUNE, DWARF_WANNABE_FORTUNE);
            }
            if (worn.keySet().containsAll(TITANIUM_SET)) {
                total.withBase(ItemStatistic.MINING_SPEED, TRUE_DWARF_SPEED);
                total.withBase(ItemStatistic.MINING_FORTUNE, TRUE_DWARF_FORTUNE);
            }
        }
        ItemStatistics result = total.build();
        if (situation.server() == ServerType.SKYBLOCK_THE_END) {
            for (Map.Entry<String, Integer> entry : END_MULTIPLIERS.entrySet()) {
                ItemStatistics base = worn.get(entry.getKey());
                if (base == null) continue;
                for (int i = 1; i < entry.getValue(); i++) result = ItemStatistics.add(result, base);
            }
        }
        if (situation.contestActive() && worn.containsKey("ZORROS_CAPE")) {
            result = ItemStatistics.add(result, worn.get("ZORROS_CAPE"));
        }
        if (situation.arthropodEnemy() && worn.containsKey("SHRIVELED_BRACELET")) {
            result = ItemStatistics.add(result, ItemStatistics.builder()
                    .withBase(ItemStatistic.COMBAT_WISDOM, SHRIVELED_COMBAT_WISDOM)
                    .withBase(ItemStatistic.MAGIC_FIND, SHRIVELED_MAGIC_FIND).build());
        }
        if (situation.region() == RegionType.MUSHROOM_DESERT && worn.containsKey("PELT_BELT")) {
            result = ItemStatistics.add(result, ItemStatistics.builder().withBase(ItemStatistic.SPEED, PELT_BELT_SPEED).build());
        }
        if (worn.containsKey("DRAGONFUSE_GLOVE") && ASPECT_OF_THE_DRAGONS.equals(situation.heldItemId())) {
            result = ItemStatistics.add(result, ItemStatistics.builder()
                    .withBase(ItemStatistic.DAMAGE, DRAGONFUSE_DAMAGE)
                    .withBase(ItemStatistic.STRENGTH, DRAGONFUSE_STRENGTH).build());
        }
        return result;
    }

    public static double blockFortune(Collection<String> wornIds, String mineableBlockName) {
        boolean mithril = mineableBlockName.startsWith("MITHRIL_");
        boolean titanium = mineableBlockName.equals("TITANIUM");
        double total = 0;
        for (String id : wornIds) {
            if (mithril && MITHRIL_SET.contains(id)) total += MITHRIL_BLOCK_FORTUNE;
            if ((mithril || titanium) && TITANIUM_SET.contains(id)) total += TITANIUM_BLOCK_FORTUNE;
        }
        return total;
    }

    public static boolean fistsBoosted(Collection<String> wornIds, boolean handEmpty) {
        return handEmpty && wornIds.contains("KNUCKLE_SANDWICH");
    }

    public static double miningFortune(SkyBlockPlayer player, MineableBlock block) {
        return blockFortune(wornItems(player).keySet(), block.name());
    }

    public static ItemStatistics statistics(SkyBlockPlayer player, @Nullable LivingEntity enemy) {
        Map<String, ItemStatistics> worn = wornItems(player);
        if (worn.isEmpty()) return ItemStatistics.empty();
        SkyBlockRegion region = player.getRegion();
        boolean arthropod = enemy instanceof SkyBlockMob mob && mob.getMobTypes().contains(MobType.ARTHROPOD);
        Situation situation = new Situation(
                HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType(),
                region == null ? null : region.getType(),
                FarmingContest.active().isPresent(),
                arthropod,
                heldId(player));
        return statistics(worn, situation);
    }

    public static boolean fistsBoosted(SkyBlockPlayer player) {
        return fistsBoosted(wornItems(player).keySet(), player.getItemInMainHand().isAir());
    }

    public static Map<String, ItemStatistics> wornItems(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return Map.of();
        var inventory = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue();
        List<SkyBlockItem> items = new ArrayList<>();
        for (var serialized : List.of(inventory.getNecklace(), inventory.getCloak(), inventory.getBelt(), inventory.getGloves())) {
            if (serialized != null) items.add(new SkyBlockItem(serialized));
        }
        Map<String, ItemStatistics> worn = new java.util.HashMap<>();
        for (SkyBlockItem item : items) {
            if (item.isNA() || item.isAir()) continue;
            String id = item.getAttributeHandler().getTypeAsString();
            if (id != null) worn.put(id, item.getAttributeHandler().getStatistics());
        }
        return worn;
    }

    private static @Nullable String heldId(SkyBlockPlayer player) {
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        return held.isAir() || held.isNA() ? null : held.getAttributeHandler().getTypeAsString();
    }
}
