package dev.daweapon.type.skyblockgeneric.hunting.creature.trigger;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.foraging.ForagingTreeManager.TreeRef;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Spawn sources for creatures whose spec carries a {@code spawn.trigger}: tree felling and Tree Gifts, honeyhives,
 * anthills, harvests, the SkyBlock hour, a timer, a replaced spawn and Honeycomb on a protected tree.
 */
public final class CreatureTriggers {
    private static final String LURKER_ATTRIBUTE = "C24";
    private static final double HONEYHIVE_SCATTER = 2.5;
    private static final double TREE_SCATTER = 3;
    private static final double HARVEST_SCATTER = 2;
    private static final Map<Instance, Pos> CURRENT_TREE = Collections.synchronizedMap(new WeakHashMap<>());

    private CreatureTriggers() {
    }

    public static void start() {
        ScheduledSpawns.start();
    }

    public static List<HuntingCreatureMob> escortOf(HuntingCreatureMob queen) {
        return AntHills.escortOf(queen);
    }

    public static void onTreeFelled(Instance instance, TreeRef tree, Map<UUID, Integer> contributions) {
        CURRENT_TREE.put(instance, tree.base());
        UUID top = contributions.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        for (CreatureSpec spec : TriggerSupport.specs("TREE_BREAK")) {
            if (isGift(spec) || !inRegion(spec, tree.base())) continue;
            if (TriggerSupport.roll(top, spec, spec.spawn().trigger().chance())) spawnByTree(spec, instance, tree.base());
        }
    }

    public static void treeFinished(Instance instance) {
        CURRENT_TREE.remove(instance);
    }

    public static void onTreeGift(SkyBlockPlayer player, String treeType) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        Pos base = CURRENT_TREE.getOrDefault(instance, player.getPosition());
        double lurker = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse(LURKER_ATTRIBUTE));
        for (CreatureSpec spec : TriggerSupport.specs("TREE_BREAK")) {
            if (!isGift(spec) || !inRegion(spec, base)) continue;
            double chance = TriggerMath.withLurker(spec.spawn().trigger().chance(), lurker);
            if (TriggerSupport.roll(player.getUuid(), spec, chance)) spawnByTree(spec, instance, base);
        }
    }

    public static void onHoneyhiveOpened(SkyBlockPlayer player, Point hive) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        for (CreatureSpec spec : TriggerSupport.specs("HONEYHIVE_OPEN")) {
            if (!TriggerSupport.hasRoom(spec) || !TriggerSupport.roll(player.getUuid(), spec, spec.spawn().trigger().chance())) continue;
            Pos center = hive.asPos().add(0.5, 0.5, 0.5);
            if (TriggerSupport.spawn(spec, instance, TriggerSupport.scatter(instance, center, spec, HONEYHIVE_SCATTER)) != null) {
                TriggerSupport.tell(player, spec, "spawn");
            }
        }
    }

    public static boolean onBlockBreak(SkyBlockPlayer player, Point position, Block block) {
        return AntHills.handle(player, position, block);
    }

    public static void onHarvest(SkyBlockPlayer player, ItemType item, Point position) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        for (CreatureSpec spec : TriggerSupport.specs("HARVEST")) {
            if (!(spec.spawn().trigger().details().get("items") instanceof List<?> items) || !items.contains(item.name())) continue;
            if (!TriggerSupport.hasRoom(spec) || !TriggerSupport.roll(player.getUuid(), spec, spec.spawn().trigger().chance())) continue;
            Pos center = position.asPos().add(0.5, 0.5, 0.5);
            TriggerSupport.spawn(spec, instance, TriggerSupport.scatter(instance, center, spec, HARVEST_SCATTER));
        }
    }

    public static CreatureSpec replacement(CreatureSpec original, @Nullable UUID nearbyPlayer) {
        for (CreatureSpec spec : TriggerSupport.specs("REPLACES")) {
            if (!original.id().equals(spec.spawn().trigger().details().get("mob")) || !TriggerSupport.hasRoom(spec)) continue;
            if (TriggerSupport.roll(nearbyPlayer, spec, spec.spawn().trigger().chance())) return spec;
        }
        return original;
    }

    private static boolean isGift(CreatureSpec spec) {
        return Boolean.TRUE.equals(spec.spawn().trigger().details().get("gift"));
    }

    private static boolean inRegion(CreatureSpec spec, Pos position) {
        if (spec.spawn().regions().isEmpty() || "TORRHUS_CANYON".equals(spec.island())) return true;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(position);
        return region != null && spec.spawn().regions().contains(region.getType());
    }

    private static void spawnByTree(CreatureSpec spec, Instance instance, Pos base) {
        TriggerSupport.spawn(spec, instance, TriggerSupport.scatter(instance, base, spec, TREE_SCATTER));
    }
}
