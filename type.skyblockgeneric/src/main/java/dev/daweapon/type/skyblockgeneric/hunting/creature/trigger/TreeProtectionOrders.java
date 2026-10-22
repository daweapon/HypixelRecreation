package dev.daweapon.type.skyblockgeneric.hunting.creature.trigger;

import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.hotf.HotfBeekeeper;
import dev.daweapon.type.skyblockgeneric.foraging.ForagingTreeManager.TreeRef;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class TreeProtectionOrders {
    private static final double SPAWN_RADIUS = 3;
    private static final long MESSAGE_COOLDOWN_MS = 2000;

    private static final Map<Long, Order> ACTIVE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_WARNING = new ConcurrentHashMap<>();

    private TreeProtectionOrders() {
    }

    private record Order(Instance instance, TreeRef tree, UUID owner) {
    }

    public static boolean isActive(TreeRef tree) {
        return ACTIVE.containsKey(tree.id());
    }

    public static boolean blocksFelling(SkyBlockPlayer player, @Nullable TreeRef tree) {
        if (tree == null || !ACTIVE.containsKey(tree.id())) return false;
        long now = System.currentTimeMillis();
        Long last = LAST_WARNING.get(player.getUuid());
        if (last == null || now - last > MESSAGE_COOLDOWN_MS) {
            LAST_WARNING.put(player.getUuid(), now);
            player.sendMessage("<c>This tree is under a Tree Protection Order! You can't cut it while it is lathered with Honeycomb.");
        }
        return true;
    }

    static List<CreatureSpec> attracted(String treeKind) {
        return TriggerSupport.specs("HONEY_ON_TREE").stream()
                .filter(spec -> treeKind.equals(spec.spawn().trigger().details().get("tree")))
                .toList();
    }

    static boolean place(SkyBlockPlayer player, TreeRef tree, int minutes) {
        Order order = new Order(player.getInstance(), tree, player.getUuid());
        if (ACTIVE.putIfAbsent(tree.id(), order) != null) return false;
        if (minutes <= 0) resolve(order);
        else {
            boolean faster = HotfBeekeeper.active(player, HotfBeekeeper.FASTER_CRITTERS);
            ScheduleUtility.delay(() -> resolve(order), (int) Math.max(1, Math.round(minutes * 60 * 20 * HotfBeekeeper.speedFactor(faster))));
        }
        return true;
    }

    private static void resolve(Order order) {
        if (ACTIVE.remove(order.tree().id(), order)) {
            List<CreatureSpec> candidates = attracted(order.tree().kind());
            if (candidates.isEmpty()) return;
            CreatureSpec chosen = LootRoll.pick(order.owner(), candidates, TriggerSupport::shardItem,
                    spec -> spec.spawn().trigger().chance(), ThreadLocalRandom.current());
            HuntingCreatureMob mob = TriggerSupport.spawn(chosen, order.instance(),
                    TriggerSupport.scatter(order.instance(), order.tree().base(), chosen, SPAWN_RADIUS));
            Player owner = order.instance().getPlayerByUuid(order.owner());
            if (owner instanceof SkyBlockPlayer player) {
                if (mob == null) player.sendMessage("<7><o>The Honeycomb on your tree went untouched...");
                else player.sendMessage("<a>A <6>{} <a>was attracted to the <6>Honeycomb <a>on your tree!", chosen.name());
                if (HotfBeekeeper.active(player, HotfBeekeeper.SECOND_CRITTER)
                        && LootRoll.roll(order.owner(), TriggerSupport.shardItem(chosen), HotfBeekeeper.SECOND_CRITTER_PERCENT)) {
                    CreatureSpec second = LootRoll.pick(order.owner(), candidates, TriggerSupport::shardItem,
                            spec -> spec.spawn().trigger().chance(), ThreadLocalRandom.current());
                    HuntingCreatureMob extra = TriggerSupport.spawn(second, order.instance(),
                            TriggerSupport.scatter(order.instance(), order.tree().base(), second, SPAWN_RADIUS));
                    if (extra != null) player.sendMessage("<a>Your <6>Beekeeper <a>luck attracted a second critter, a <6>{}<a>!", second.name());
                }
            }
        }
    }
}
