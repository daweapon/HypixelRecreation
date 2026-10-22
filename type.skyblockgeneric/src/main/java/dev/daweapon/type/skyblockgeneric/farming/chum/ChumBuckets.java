package dev.daweapon.type.skyblockgeneric.farming.chum;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.equipment.EquipmentBonuses;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ChumBuckets {
    public static final double NEARBY_RANGE = 16;
    public static final double LEAVE_RANGE = 40;
    public static final int MAX_BONUS_BUCKETS = 4;
    private static final double COINS_PER_CHUM = 20;
    private static final double FISHING_XP_PER_CHUM = 10;

    private static final Map<UUID, ChumBucket> PLACED = new ConcurrentHashMap<>();
    private static final AtomicBoolean STARTED = new AtomicBoolean();

    private ChumBuckets() {
    }

    public static boolean hasPlaced(SkyBlockPlayer player) {
        return PLACED.containsKey(player.getUuid());
    }

    public static boolean place(SkyBlockPlayer player, Instance instance, Pos position) {
        if (hasPlaced(player)) {
            player.sendMessage("<c>You already have a bucket placed out there. Fill that one first!");
            return false;
        }
        PLACED.put(player.getUuid(), new ChumBucket(player, instance, position));
        player.sendMessage("<a>You placed down a Chum Bucket!");
        start();
        return true;
    }

    public static int bucketsNear(Instance instance, Pos position) {
        int count = 0;
        for (ChumBucket bucket : PLACED.values()) {
            if (bucket.instance() == instance && bucket.position().distance(position) <= NEARBY_RANGE) count++;
        }
        return Math.min(count, MAX_BONUS_BUCKETS);
    }

    public static void clicked(SkyBlockPlayer player, ChumBucket bucket) {
        boolean own = bucket.ownerId().equals(player.getUuid());
        if (own && bucket.isFull()) {
            collect(player, bucket);
            return;
        }
        int held = player.getAmountAnywhere(ItemType.CHUM);
        if (held <= 0 || bucket.isFull()) {
            player.sendMessage("<e>Deposit <a>Chums<e> in this bucket!");
            if (!own) player.sendMessage("<e>Get your own bucket in the <2>Glowing Mushroom Cave<e>, <a>in The Farming Islands<e>.");
            return;
        }
        int amount = Math.min(held, bucket.room());
        if (amount <= 0 || !player.removeItemFromPlayer(ItemType.CHUM, amount)) return;
        bucket.add(amount);
        reward(player, amount, own);
        if (own) {
            player.sendMessage("<e>You deposited <a>{} Chums<e> in your own bucket!", amount);
        } else {
            player.sendMessage("<e>You deposited <a>{} Chums<e> in <7>{}<e>'s bucket!", amount, bucket.ownerName());
            SkyBlockPlayer owner = onlineOwner(bucket);
            if (owner != null) owner.sendMessage("<7>{}<e> deposited <a>{} Chums<e> in your bucket!", player.getUsername(), amount);
        }
    }

    private static void reward(SkyBlockPlayer player, int amount, boolean own) {
        double coins = COINS_PER_CHUM * amount;
        double xp = FISHING_XP_PER_CHUM * amount;
        if (!own && EquipmentBonuses.wornItems(player).containsKey("LUMINOUS_BRACELET")) {
            coins *= 2;
            xp *= 2;
        }
        player.addCoins(coins);
        player.getSkills().increase(player, SkillCategories.FISHING, xp);
    }

    private static void collect(SkyBlockPlayer player, ChumBucket bucket) {
        if (!player.removeItemFromPlayer(ItemType.EMPTY_CHUM_BUCKET, 1)) {
            player.sendMessage("<c>You need the Empty Chum Bucket you placed in your inventory to collect it!");
            return;
        }
        PLACED.remove(bucket.ownerId(), bucket);
        bucket.remove();
        player.addAndUpdateItem(ItemType.FULL_CHUM_BUCKET, 1);
        player.sendMessage("<a>You collected your filled Chum Bucket!");
    }

    private static SkyBlockPlayer onlineOwner(ChumBucket bucket) {
        for (var player : bucket.instance().getPlayers()) {
            if (player.getUuid().equals(bucket.ownerId()) && player instanceof SkyBlockPlayer skyBlockPlayer) return skyBlockPlayer;
        }
        return null;
    }

    private static void start() {
        if (!STARTED.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager().buildTask(ChumBuckets::tick)
                .delay(TaskSchedule.seconds(2))
                .repeat(TaskSchedule.seconds(2))
                .schedule();
    }

    private static void tick() {
        for (ChumBucket bucket : List.copyOf(PLACED.values())) {
            SkyBlockPlayer owner = onlineOwner(bucket);
            if (owner == null) {
                drop(bucket);
                continue;
            }
            if (owner.getPosition().distance(bucket.position()) > LEAVE_RANGE) {
                drop(bucket);
                owner.sendMessage("<e>Automatically picked up the Chum Bucket you left back there!");
            }
        }
    }

    private static void drop(ChumBucket bucket) {
        PLACED.remove(bucket.ownerId(), bucket);
        bucket.remove();
    }
}
