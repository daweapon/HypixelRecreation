package dev.daweapon.type.skyblockgeneric.moonglade.flames;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BlueFlamesTrial {
    public static final String MET_TRIGGER = "brian.met";
    public static final String DONE_TRIGGER = "brian.trials_done";
    public static final String BADGE_TRIGGER = "brian.badge_level";
    public static final String TIER_ONE_TALK_TRIGGER = "brian.talked_tier1";
    private static final long NOTICE_COOLDOWN_MILLIS = 6_000;

    public enum Outcome { NOTHING_PENDING, GIVEN_FIRST, UPGRADED, NEEDS_BADGE_IN_HAND, NEEDS_MATERIALS }

    private static final AtomicBoolean STARTED = new AtomicBoolean();
    private static final Map<UUID, Boolean> RUNNING = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_NOTICE = new ConcurrentHashMap<>();

    private BlueFlamesTrial() {
    }

    public static void start() {
        if (!STARTED.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (HypixelConst.getTypeLoader() == null || HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_GALATEA) return;
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                try {
                    if (!RUNNING.containsKey(player.getUuid()) && inSoulCampfire(player)) attempt(player);
                } catch (RuntimeException ignored) {
                }
            }
        }, TaskSchedule.seconds(1), TaskSchedule.millis(200));
    }

    public static boolean inSoulCampfire(SkyBlockPlayer player) {
        return player.getInstance() != null
                && player.getInstance().getBlock(player.getPosition().sub(0, 0.3, 0)).compare(Block.SOUL_CAMPFIRE);
    }

    public static boolean met(SkyBlockPlayer player) {
        return ChapterTriggers.done(player, MET_TRIGGER);
    }

    public static void markMet(SkyBlockPlayer player) {
        ChapterTriggers.fire(player, MET_TRIGGER);
    }

    public static int completed(SkyBlockPlayer player) {
        return (int) Math.min(BlueFlamesTrials.TRIAL_COUNT, ChapterService.data(player).trigger(DONE_TRIGGER));
    }

    public static int claimed(SkyBlockPlayer player) {
        return (int) Math.min(BlueFlamesTrials.BADGE_COUNT, ChapterService.data(player).trigger(BADGE_TRIGGER));
    }

    public static boolean badgePending(SkyBlockPlayer player) {
        return BlueFlamesTrials.badgeLevelAfter(completed(player)) > claimed(player);
    }

    private static void notice(SkyBlockPlayer player, String message) {
        long now = System.currentTimeMillis();
        Long last = LAST_NOTICE.get(player.getUuid());
        if (last != null && now - last < NOTICE_COOLDOWN_MILLIS) return;
        LAST_NOTICE.put(player.getUuid(), now);
        trialMessage(player, message);
    }

    private static void trialMessage(SkyBlockPlayer player, String message, Object... args) {
        player.sendMessage("<3><l>TRIAL OF BLUE FLAMES! </l><f>" + message, args);
    }

    private static void attempt(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return;
        if (!met(player)) {
            notice(player, "<7>Talk to <3>Brian <7>to learn about the trial first.");
            return;
        }
        int done = completed(player);
        if (done >= BlueFlamesTrials.TRIAL_COUNT) {
            notice(player, "<a>You already completed every Trial of Blue Flames!");
            return;
        }
        if (badgePending(player)) {
            notice(player, "<c>Talk to <3>Brian <c>to claim your Soul Campfire Badge upgrade first!");
            return;
        }
        begin(player, done + 1);
    }

    private static void begin(SkyBlockPlayer player, int trial) {
        RUNNING.put(player.getUuid(), true);
        int[] elapsed = {0};
        MinecraftServer.getSchedulerManager().submitTask(() -> tick(player, trial, elapsed), ExecutionType.TICK_END);
    }

    private static TaskSchedule tick(SkyBlockPlayer player, int trial, int[] elapsed) {
        UUID id = player.getUuid();
        if (player.getInstance() == null || !inSoulCampfire(player)) {
            RUNNING.remove(id);
            trialMessage(player, "<c>Trial cancelled: <f>You stepped out of the campfire!");
            return TaskSchedule.stop();
        }
        double dps = BlueFlamesTrials.damagePerSecond(trial);
        if (elapsed[0] == 0) {
            if (BlueFlamesTrials.instantFail(dps, player.getMaxHealth())) {
                RUNNING.remove(id);
                trialMessage(player, "<c>The flames burn too hot! <f>You need more than <c>{} ❤ Health <f>to survive Trial {}.",
                        StringUtility.commaify((int) dps), StringUtility.getAsRomanNumeral(trial));
                return TaskSchedule.stop();
            }
            trialMessage(player, "<f>Started <3>Trial of Blue Flames {}<f>! <7>Survive <a>{}s<7>.",
                    StringUtility.getAsRomanNumeral(trial), BlueFlamesTrials.SURVIVE_SECONDS);
            player.setFireTicks(30);
            elapsed[0] = 1;
            return TaskSchedule.seconds(1);
        }
        double defense = player.getStatistics().allStatistics().getOverall(ItemStatistic.DEFENSE);
        float damage = (float) BlueFlamesTrials.damageAfterDefense(dps, defense);
        player.setFireTicks(30);
        player.damage(new Damage(DamageType.IN_FIRE, null, null, player.getPosition(), damage));
        if (elapsed[0] >= BlueFlamesTrials.SURVIVE_SECONDS) {
            RUNNING.remove(id);
            complete(player, trial);
            return TaskSchedule.stop();
        }
        elapsed[0]++;
        return TaskSchedule.seconds(1);
    }

    private static void complete(SkyBlockPlayer player, int trial) {
        player.setFireTicks(0);
        ChapterTriggers.atLeast(player, DONE_TRIGGER, trial);
        if (trial >= 10) ChapterTriggers.atLeast(player, BlueFlamesTrials.TIER_TEN_TRIGGER, 1);
        player.showTitle(Text.of("<a>Complete!"), Text.literal(" "),
                Title.Times.times(Duration.ofSeconds(1), Duration.ZERO, Duration.ofSeconds(1)));
        trialMessage(player, "<a>Completed <3>Trial of Blue Flames {}<a>!", StringUtility.getAsRomanNumeral(trial));
    }

    public static Outcome claimBadge(SkyBlockPlayer player) {
        int claimed = claimed(player);
        int target = claimed + 1;
        if (BlueFlamesTrials.badgeLevelAfter(completed(player)) < target) return Outcome.NOTHING_PENDING;
        if (claimed == 0) {
            player.addAndUpdateItem(ItemType.get(BlueFlamesTrials.badgeId(target)));
            ChapterTriggers.atLeast(player, BADGE_TRIGGER, target);
            return Outcome.GIVEN_FIRST;
        }
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        String heldId = held.isNA() || held.isAir() ? null : held.getAttributeHandler().getTypeAsString();
        if (BlueFlamesTrials.levelOfBadge(heldId) != claimed) return Outcome.NEEDS_BADGE_IN_HAND;
        BlueFlamesTrials.Upgrade cost = BlueFlamesTrials.upgradeInto(target);
        if (cost != null && !pay(player, cost)) return Outcome.NEEDS_MATERIALS;
        player.setItemInMainHand(ItemStack.AIR);
        player.addAndUpdateItem(ItemType.get(BlueFlamesTrials.badgeId(target)));
        ChapterTriggers.atLeast(player, BADGE_TRIGGER, target);
        return Outcome.UPGRADED;
    }

    private static boolean pay(SkyBlockPlayer player, BlueFlamesTrials.Upgrade cost) {
        ItemType type = ItemType.get(cost.itemId());
        if (type == null || player.countItem(type) < cost.amount()) return false;
        return player.takeItem(type, cost.amount()) != null;
    }

    public static void maxOut(SkyBlockPlayer player) {
        if (claimed(player) < BlueFlamesTrials.BADGE_COUNT) player.addAndUpdateItem(ItemType.get(BlueFlamesTrials.badgeId(BlueFlamesTrials.BADGE_COUNT)));
        ChapterTriggers.atLeast(player, DONE_TRIGGER, BlueFlamesTrials.TRIAL_COUNT);
        ChapterTriggers.atLeast(player, BADGE_TRIGGER, BlueFlamesTrials.BADGE_COUNT);
        ChapterTriggers.atLeast(player, BlueFlamesTrials.TIER_TEN_TRIGGER, 1);
        ChapterTriggers.atLeast(player, MET_TRIGGER, 1);
    }
}
