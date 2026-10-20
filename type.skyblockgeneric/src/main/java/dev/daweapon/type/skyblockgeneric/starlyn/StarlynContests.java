package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointStarlyn;
import dev.daweapon.type.skyblockgeneric.event.custom.FishCaughtEvent;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynProfile.Entry;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynProfile.SisterData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.ToDoubleFunction;

public final class StarlynContests {
    public static final String SWOOP = "npc.swoop";
    public static final String DAVID = "npc.david";
    public static final String KYSHA = "npc.kysha";

    private static final AtomicBoolean STARTED = new AtomicBoolean();
    private static final List<ToDoubleFunction<SkyBlockPlayer>> POINT_MULTIPLIERS = new CopyOnWriteArrayList<>();
    private static final List<ToDoubleFunction<SkyBlockPlayer>> EXTRA_TICKET_CHANCES = new CopyOnWriteArrayList<>();

    private StarlynContests() {
    }

    public enum Gate {
        NEEDS_HOTF, NEEDS_SWOOP, NEEDS_DAVID, NEEDS_KYSHA, READY
    }

    public static void start() {
        if (!STARTED.compareAndSet(false, true)) return;
        addPointMultiplier(player -> 1 + HotfService.perkValue(player, "starlyn_supreme") / 100D);
        addExtraTicketChance(player -> HotfService.perkValue(player, "two_for_one"));
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getSkyblockDataHandler() == null) continue;
                try {
                    finaliseEnded(player);
                } catch (RuntimeException exception) {
                    Logger.error(exception, "Starlyn contest results failed for {}", player.getUsername());
                }
            }
            return TaskSchedule.seconds(5);
        });
    }

    public static long currentDay() {
        return SkyBlockCalendar.getElapsed() / SkyBlockCalendar.DAY;
    }

    public static String dateLabel(long day) {
        long start = day * SkyBlockCalendar.DAY;
        int year = (int) (start / SkyBlockCalendar.YEAR) + 1;
        return SkyBlockCalendar.getMonthName(start) + " " + StringUtility.ntify(SkyBlockCalendar.getDay(start)) + ", Year " + year;
    }

    public static StarlynProfile profile(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static SisterData data(SkyBlockPlayer player, StarlynSister sister) {
        return profile(player).sister(sister);
    }

    public static void save(SkyBlockPlayer player, StarlynProfile profile) {
        datapoint(player).setValue(profile);
    }

    private static DatapointStarlyn datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.STARLYN, DatapointStarlyn.class);
    }

    public static @Nullable StarlynSister islandSister() {
        if (HypixelConst.getTypeLoader() == null) return null;
        String type = HypixelConst.getTypeLoader().getType().name();
        for (StarlynSister sister : StarlynSister.values()) {
            if (StarlynConfig.get(sister).serverType().equals(type)) return sister;
        }
        return null;
    }

    public static Gate gate(SkyBlockPlayer player) {
        if (!HotfService.isUnlocked(player)) return Gate.NEEDS_HOTF;
        if (!ChapterTriggers.done(player, SWOOP)) return Gate.NEEDS_SWOOP;
        if (!ChapterTriggers.done(player, DAVID)) return Gate.NEEDS_DAVID;
        if (!ChapterTriggers.done(player, KYSHA)) return Gate.NEEDS_KYSHA;
        return Gate.READY;
    }

    public static boolean canParticipate(SkyBlockPlayer player, StarlynSister sister) {
        return player.getSkyblockDataHandler() != null && gate(player) == Gate.READY && data(player, sister).isMet();
    }

    public static void markMet(SkyBlockPlayer player, StarlynSister sister) {
        StarlynProfile profile = profile(player);
        profile.sister(sister).setMet(true);
        save(player, profile);
    }

    public static void addPointMultiplier(ToDoubleFunction<SkyBlockPlayer> factor) {
        POINT_MULTIPLIERS.add(factor);
    }

    public static void addExtraTicketChance(ToDoubleFunction<SkyBlockPlayer> percent) {
        EXTRA_TICKET_CHANCES.add(percent);
    }

    public static double pointMultiplier(SkyBlockPlayer player) {
        double multiplier = 1;
        for (ToDoubleFunction<SkyBlockPlayer> factor : POINT_MULTIPLIERS) multiplier *= factor.applyAsDouble(player);
        return multiplier;
    }

    public static double extraTicketChance(SkyBlockPlayer player) {
        double percent = 0;
        for (ToDoubleFunction<SkyBlockPlayer> chance : EXTRA_TICKET_CHANCES) percent += chance.applyAsDouble(player);
        return percent;
    }

    public static void recordTreeGift(SkyBlockPlayer player) {
        StarlynSister sister = islandSister();
        if (sister == null) return;
        score(player, sister, _ -> StarlynScoring.of(sister).treeGift());
    }

    public static void recordLogs(SkyBlockPlayer player, StarlynTree tree, int amount) {
        StarlynSister sister = islandSister();
        if (sister == null || amount <= 0) return;
        int perHundred = StarlynScoring.of(sister).logsPerHundred(tree);
        if (perHundred <= 0) return;
        score(player, sister, entry -> {
            long before = entry.logs(tree);
            long after = before + amount;
            entry.getLogs().put(tree, after);
            return (double) (after / 100 - before / 100) * perHundred;
        });
    }

    public static void recordHunt(SkyBlockPlayer player, AttributeDefinition shard) {
        StarlynSister sister = islandSister();
        if (sister == null) return;
        score(player, sister, _ -> StarlynScoring.of(sister).hunting(shard.shard(), shard.rarity().name()));
    }

    public static void recordFishing(SkyBlockPlayer player, FishCaughtEvent.Kind kind, @Nullable String quality) {
        StarlynSister sister = islandSister();
        if (sister == null) return;
        score(player, sister, _ -> StarlynScoring.of(sister).fishing(kind, quality));
    }

    public static void recordKill(SkyBlockPlayer player) {
        StarlynSister sister = islandSister();
        if (sister == null) return;
        score(player, sister, _ -> StarlynScoring.of(sister).combat());
    }

    public static void recordGather(SkyBlockPlayer player, StarlynResource resource) {
        if (islandSister() != resource.getSister()) return;
        score(player, resource.getSister(), _ -> StarlynScoring.of(resource.getSister()).gathering(resource));
    }

    private static void score(SkyBlockPlayer player, StarlynSister sister, ToDoubleFunction<Entry> gain) {
        if (!canParticipate(player, sister)) return;
        StarlynProfile profile = profile(player);
        SisterData data = profile.sister(sister);
        long day = currentDay();
        Entry entry = data.entryFor(day).orElseGet(() -> {
            Entry created = new Entry(day);
            data.getEntries().add(created);
            return created;
        });
        double points = gain.applyAsDouble(entry) * pointMultiplier(player);
        if (points > 0) entry.setPoints(entry.getPoints() + points);
        if (entry.getPoints() > 0 && !data.isParticipated()) {
            data.setParticipated(true);
            ChapterTriggers.fire(player, sister.trigger("participated"));
        }
        save(player, profile);
    }

    public static void finaliseEnded(SkyBlockPlayer player) {
        StarlynProfile profile = profile(player);
        long today = currentDay();
        boolean changed = false;
        for (StarlynSister sister : StarlynSister.values()) {
            SisterData data = profile.sister(sister);
            for (Entry entry : List.copyOf(data.getEntries())) {
                if (entry.isFinalised() || entry.getDay() >= today) continue;
                finalise(player, sister, data, entry);
                changed = true;
            }
            if (changed) data.trimHistory();
        }
        if (changed) save(player, profile);
    }

    private static void finalise(SkyBlockPlayer player, StarlynSister sister, SisterData data, Entry entry) {
        StarlynConfig.BracketReward reward = StarlynConfig.get(sister).bracketFor(entry.score());
        if (reward == null) {
            data.getEntries().remove(entry);
            return;
        }
        StarlynBracket bracket = reward.bracket();
        entry.setBracket(bracket);
        entry.setFinalised(true);
        boolean firstTime = !data.getReached().contains(bracket);
        data.getReached().add(bracket);
        StarlynPersonalBests.update(player, sister, data, entry);
        ChapterTriggers.atLeast(player, sister.trigger("bracket"), bracket.triggerValue());
        announce(player, sister, entry, bracket);
        if (firstTime && bracket.isHidden()) StarlynDialogue.hiddenBracket(sister, bracket).forEach(line -> player.sendMessage(line));
    }

    private static void announce(SkyBlockPlayer player, StarlynSister sister, Entry entry, StarlynBracket bracket) {
        if (hasContact(player, sister)) {
            ring(player, sister);
            return;
        }
        player.sendMessage(StarlynDialogue.speaker(sister) + "The contest is over! You reached the " + bracket.formatted()
                + " <r><f>bracket with <a>" + StringUtility.commaify(entry.score()) + " points<f>! Come see me to claim your <6>rewards<f>.");
    }

    public static boolean hasContact(SkyBlockPlayer player, StarlynSister sister) {
        AbiphoneNPC contact = AbiphoneRegistry.getFromId(sister.key());
        if (contact == null) return false;
        for (SkyBlockItem item : player.getAllInventoryItems()) {
            if (item.hasComponent(AbiphoneComponent.class) && item.getAttributeHandler().hasAbiphoneNPC(contact)) return true;
        }
        return false;
    }

    private static void ring(SkyBlockPlayer player, StarlynSister sister) {
        player.sendMessage("<b>✆ " + sister.npcName() + " <f>is calling... <7>Pick up with your Abiphone!");
        for (int pulse = 0; pulse < 3; pulse++) {
            ScheduleUtility.delay(() -> player.playSound(Sound.sound(Key.key("block.note_block.pling"),
                    Sound.Source.PLAYER, 1f, 1.6f), Sound.Emitter.self()), pulse * 8);
        }
    }

    public static int claim(SkyBlockPlayer player, StarlynSister sister, Collection<Long> days) {
        int claimed = 0;
        SisterData data = data(player, sister);
        for (Entry entry : data.claimable().stream().sorted((a, b) -> Long.compare(a.getDay(), b.getDay())).toList()) {
            if (!days.contains(entry.getDay())) continue;
            StarlynBracket bracket = entry.getBracket();
            StarlynRewards.grant(player, sister, bracket);
            boolean bonus = StarlynRewards.claimDailyBonus(player, sister);
            entry.setClaimed(true);
            player.sendMessage("<a>You claimed the " + bracket.formatted() + " <r><a>rewards from the <e>" + sister.contestName()
                    + " <a>on <e>" + dateLabel(entry.getDay()) + "<a>!" + (bonus ? " <e>(Daily Bonus)" : ""));
            claimed++;
        }
        if (claimed > 0) {
            StarlynProfile profile = profile(player);
            profile.sister(sister).trimHistory();
            save(player, profile);
        }
        return claimed;
    }

    public static int claimAll(SkyBlockPlayer player, StarlynSister sister) {
        return claim(player, sister, data(player, sister).claimable().stream().map(Entry::getDay).toList());
    }

    public static void fillRewards(SkyBlockPlayer player, StarlynSister sister, int count) {
        for (int i = 0; i < count; i++) StarlynRewards.grant(player, sister, StarlynBracket.SPECIAL);
    }
}
