package dev.daweapon.type.skyblockgeneric.chapters;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChapters;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.hotf.HotfCurrency;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChapterService {
    private static final Set<UUID> EVALUATING = ConcurrentHashMap.newKeySet();

    private ChapterService() {
    }

    private static boolean ready(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler() != null;
    }

    private static DatapointChapters datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHAPTERS, DatapointChapters.class);
    }

    public static DatapointChapters.ChaptersData data(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static void add(SkyBlockPlayer player, String trigger, long amount) {
        if (!ready(player) || amount <= 0) return;
        DatapointChapters.ChaptersData data = data(player);
        data.getTriggers().merge(trigger, amount, Long::sum);
        datapoint(player).setValue(data);
        evaluate(player);
    }

    public static void atLeast(SkyBlockPlayer player, String trigger, long value) {
        if (!ready(player)) return;
        DatapointChapters.ChaptersData data = data(player);
        if (data.trigger(trigger) >= value) return;
        data.getTriggers().put(trigger, value);
        datapoint(player).setValue(data);
        evaluate(player);
    }

    public static boolean done(SkyBlockPlayer player, String trigger) {
        return ready(player) && data(player).trigger(trigger) > 0;
    }

    public static long progress(SkyBlockPlayer player, ChapterTask task) {
        if (isDone(player, task)) return task.amount();
        long value = task.trigger() == null ? 0 : data(player).trigger(task.trigger());
        if (task.probe() != null) value = Math.max(value, task.probe().value(player));
        return Math.min(task.amount(), value);
    }

    public static boolean isDone(SkyBlockPlayer player, ChapterTask task) {
        return data(player).getTasks().contains(task.id());
    }

    public static boolean isComplete(SkyBlockPlayer player, Chapter chapter) {
        return data(player).getChapters().contains(chapter.key());
    }

    public static boolean isOpen(SkyBlockPlayer player, ChapterIsland island) {
        if (!HotfService.isUnlocked(player)) return false;
        return island.gateTrigger() == null || data(player).trigger(island.gateTrigger()) > 0;
    }

    public static boolean isUnlocked(SkyBlockPlayer player, Chapter chapter) {
        if (!isOpen(player, chapter.island())) return false;
        return chapter.number() == 1 || data(player).getChapters().contains(chapter.key() - 1);
    }

    public static boolean allComplete(SkyBlockPlayer player) {
        return allComplete(player, ChapterIsland.MOONGLADE_MARSH);
    }

    public static boolean allComplete(SkyBlockPlayer player, ChapterIsland island) {
        return ready(player) && ChapterRegistry.of(island).chapters().stream().allMatch(chapter -> isComplete(player, chapter));
    }

    public static long tasksDone(SkyBlockPlayer player, Chapter chapter) {
        return chapter.availableTasks().stream().filter(task -> isDone(player, task)).count();
    }

    public static void evaluate(SkyBlockPlayer player) {
        if (!ready(player) || !HotfService.isUnlocked(player)) return;
        if (!EVALUATING.add(player.getUuid())) return;
        try {
            for (ChapterRegistry registry : ChapterRegistry.all()) {
                if (isOpen(player, registry.island()) && !allComplete(player, registry.island())) evaluate(player, registry);
            }
        } finally {
            EVALUATING.remove(player.getUuid());
        }
    }

    private static void evaluate(SkyBlockPlayer player, ChapterRegistry registry) {
        for (Chapter chapter : registry.chapters()) {
            if (isComplete(player, chapter)) continue;
            for (ChapterTask task : chapter.availableTasks()) {
                if (isDone(player, task) || progress(player, task) < task.amount()) continue;
                markTask(player, task);
                completeTask(player, task);
            }
            if (tasksDone(player, chapter) < chapter.availableTasks().size()) return;
            markChapter(player, chapter);
            completeChapter(player, chapter);
        }
    }

    public static void maxOut(SkyBlockPlayer player) {
        DatapointChapters.ChaptersData data = data(player);
        for (ChapterRegistry registry : ChapterRegistry.all()) {
            if (registry.island().gateTrigger() != null) data.getTriggers().putIfAbsent(registry.island().gateTrigger(), 1L);
            for (Chapter chapter : registry.chapters()) {
                chapter.availableTasks().forEach(task -> data.getTasks().add(task.id()));
                data.getChapters().add(chapter.key());
            }
        }
        datapoint(player).setValue(data);
        syncSkyBlockXp(player);
    }

    public static void syncSkyBlockXp(SkyBlockPlayer player) {
        int xp = 0;
        for (ChapterRegistry registry : ChapterRegistry.all()) {
            for (Chapter chapter : registry.chapters()) {
                if (isComplete(player, chapter)) xp += chapter.completion().skyBlockXp();
            }
        }
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.CHAPTERS, xp);
    }

    private static void markTask(SkyBlockPlayer player, ChapterTask task) {
        DatapointChapters.ChaptersData data = data(player);
        data.getTasks().add(task.id());
        datapoint(player).setValue(data);
    }

    private static void markChapter(SkyBlockPlayer player, Chapter chapter) {
        DatapointChapters.ChaptersData data = data(player);
        data.getChapters().add(chapter.key());
        datapoint(player).setValue(data);
    }

    private static void completeTask(SkyBlockPlayer player, ChapterTask task) {
        grant(player, task.reward());
        player.sendMessage("<2><l>CHAPTER TASK COMPLETE! <r><a>{}", task.plainTitle());
        task.reward().lines().forEach(line -> player.sendMessage(Text.read("  " + line)));
    }

    private static void completeChapter(SkyBlockPlayer player, Chapter chapter) {
        grant(player, chapter.completion());
        syncSkyBlockXp(player);
        String island = chapter.island().displayName();
        player.sendMessage("<2><l>CHAPTER COMPLETE! <r><2>{} <7>- <a>Chapter {}", island, chapter.roman());
        chapter.completion().lines().forEach(line -> player.sendMessage(Text.read("  " + line)));
        if (chapter.number() < ChapterRegistry.of(chapter.island()).chapters().size()) {
            player.sendMessage("<a>Chapter {} tasks unlocked!", Chapter.ROMAN[chapter.number() + 1]);
        } else {
            player.sendMessage("<a>You completed every Chapter of the <2>{}<a>!", island);
        }
    }

    private static void grant(SkyBlockPlayer player, ChapterReward reward) {
        if (reward.hotf() > 0) HotfService.addExperience(player, reward.hotf());
        reward.skills().forEach((skill, amount) ->
                player.getSkills().setRaw(player, skill, player.getSkills().getRaw(skill) + amount));
        if (reward.forestEssence() > 0) Essences.add(player, EssenceType.FOREST, reward.forestEssence());
        reward.shards().forEach((name, amount) -> {
            AttributeDefinition shard = AttributeRegistry.findByShard(name).orElseThrow();
            player.getHuntingData().addShards(shard.id(), amount);
        });
        reward.items().forEach((item, amount) -> player.addAndUpdateItem(item, amount));
        if (reward.safariEssence() > 0) Essences.add(player, EssenceType.SAFARI, reward.safariEssence());
        if (reward.whispers() > 0 || reward.desertWhispers() > 0) {
            HotfService.addWhispers(player, HotfCurrency.FOREST, reward.whispers());
            HotfService.addWhispers(player, HotfCurrency.DESERT, reward.desertWhispers());
            HotfService.syncSkyBlockXp(player);
        }
    }
}
