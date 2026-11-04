package dev.daweapon.type.skyblockgeneric.theend;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobNestEndermite;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.tool.ToolPassives;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Ender Nodes (wiki: Ender_Nodes): End Stone and Obsidian blocks shown as purple terracotta. Mining one rolls the
 * node table for its original block, leaves bedrock for 5 seconds, then the original block returns.
 */
public final class EnderNodes {
    public static final int PER_TYPE = 18;
    public static final int BEDROCK_TICKS = 100;
    public static final Block NODE_BLOCK = Block.PURPLE_TERRACOTTA;
    public static final int MIN_ENDERMITES = 2;
    public static final int MAX_ENDERMITES = 4;

    public record Node(Pos position, EnderNodeType type) {
    }

    private static final Map<Long, Node> NODES = new ConcurrentHashMap<>();
    private static final Set<Long> COOLING = ConcurrentHashMap.newKeySet();

    private EnderNodes() {
    }

    public static Collection<Node> nodes() {
        return NODES.values();
    }

    public static int count(EnderNodeType type) {
        return (int) NODES.values().stream().filter(node -> node.type() == type).count();
    }

    public static boolean isNode(Point position) {
        return NODES.containsKey(key(position));
    }

    public static boolean isCoolingDown(Point position) {
        return COOLING.contains(key(position));
    }

    public static boolean place(Instance instance, Pos position, EnderNodeType type) {
        long key = key(position);
        if (NODES.containsKey(key) || COOLING.contains(key)) return false;
        if (!instance.isChunkLoaded(position) || !instance.getBlock(position).compare(type.block())) return false;
        instance.setBlock(position, NODE_BLOCK);
        NODES.put(key, new Node(position, type));
        return true;
    }

    public static void prune(Instance instance) {
        NODES.values().removeIf(node -> !instance.isChunkLoaded(node.position())
                || !instance.getBlock(node.position()).compare(NODE_BLOCK));
    }

    public static boolean handle(SkyBlockPlayer player, Point position, Block block) {
        if (!block.compare(NODE_BLOCK)) return false;
        long key = key(position);
        Node node = NODES.remove(key);
        if (node == null) return false;
        Instance instance = player.getInstance();
        if (instance == null) return true;
        COOLING.add(key);
        instance.setBlock(position, Block.BEDROCK);
        ScheduleUtility.delay(() -> {
            instance.setBlock(position, node.type().block());
            COOLING.remove(key);
        }, BEDROCK_TICKS);
        mine(player, instance, node);
        return true;
    }

    private static void mine(SkyBlockPlayer player, Instance instance, Node node) {
        player.getAchievementHandler().completeAchievement("skyblock.oh_shiny");
        boolean stonk = ToolPassives.holding(player, "STONK_PICKAXE");
        EnderNodeLoot.Entry entry = EnderNodeLoot.roll(player.getUuid(), node.type(), stonk);
        List<SkyBlockItem> drops = new ArrayList<>();
        if (entry.endermites()) {
            spawnEndermites(player, instance, node.position());
        } else {
            SkyBlockItem item = new SkyBlockItem(entry.item(), entry.amount());
            drops.add(item);
            String color = item.getAttributeHandler().getRarity().getColor().asHexString();
            String amount = entry.amount() > 1 ? "<8>" + entry.amount() + "x " : "";
            player.sendMessage("<5><l>ENDER NODE!</l> <f>You found " + amount + "<color:" + color + ">"
                    + item.getDisplayName() + "<f>!");
        }
        HypixelEventHandler.callCustomEvent(new CustomBlockBreakEvent(player, node.type().material(), node.position(), drops, false));
        drops.forEach(drop -> deliver(player, drop));
    }

    private static void spawnEndermites(SkyBlockPlayer player, Instance instance, Pos position) {
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(position);
        RegionType home = region == null ? RegionType.DRAGONS_NEST : region.getType();
        int count = ThreadLocalRandom.current().nextInt(MIN_ENDERMITES, MAX_ENDERMITES + 1) + miteBaitBonus(player);
        Pos spawn = position.add(0.5, 1, 0.5);
        for (int i = 0; i < count; i++) new MobNestEndermite(home).setInstance(instance, spawn);
        player.sendMessage("<5><l>ENDER NODE!</l> <f>You found <c>Endermite Nest<f>!");
    }

    static int miteBaitBonus(SkyBlockPlayer player) {
        double perPetLuck = PetAbilities.value(player, ItemType.ENDERMITE_PET, "Mite Bait", 0);
        if (perPetLuck <= 0) return 0;
        double chance = perPetLuck * player.getStatistics().allStatistics().getOverall(ItemStatistic.PET_LUCK);
        int bonus = (int) (chance / 100);
        if (LootRoll.roll(player.getUuid(), null, chance % 100)) bonus++;
        return bonus;
    }

    private static void deliver(SkyBlockPlayer player, SkyBlockItem item) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        if (type != null && player.canInsertItemIntoSacks(type, item.getAmount())) {
            player.getSackItems().increase(type, item.getAmount());
            return;
        }
        player.addAndUpdateItem(item);
    }

    private static long key(Point position) {
        return ((long) position.blockX() & 0x3FFFFFFL) << 38 | ((long) position.blockZ() & 0x3FFFFFFL) << 12
                | (position.blockY() & 0xFFFL);
    }
}
