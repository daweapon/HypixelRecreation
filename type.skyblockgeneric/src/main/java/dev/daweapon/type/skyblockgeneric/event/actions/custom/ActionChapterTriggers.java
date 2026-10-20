package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterProbe;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.TreeGiftReceivedEvent;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.tinylog.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class ActionChapterTriggers implements HypixelEventClass {
    private static final AtomicBoolean POLLING = new AtomicBoolean();

    public ActionChapterTriggers() {
        if (!POLLING.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            for (var player : SkyBlockGenericLoader.getLoadedPlayers()) {
                try {
                    ChapterService.evaluate(player);
                } catch (RuntimeException exception) {
                    Logger.error(exception, "Chapter evaluation failed for {}", player.getUsername());
                }
            }
            return TaskSchedule.seconds(5);
        });
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onShard(ShardCaughtEvent event) {
        AttributeDefinition shard = event.getShard();
        var player = event.getPlayer();
        ChapterTriggers.add(player, ChapterProbe.shardTrigger(shard.shard()), event.getAmount());
        if (event.isSaltActive()) ChapterTriggers.add(player, "shard.salt", event.getAmount());
        switch (event.getSource()) {
            case HUNTRAP -> ChapterTriggers.add(player, "shard.huntrap", event.getAmount());
            case FUSION -> {
                ChapterTriggers.fire(player, "fusion.use");
                if (shard.family() == AttributeDefinition.AttributeFamily.ELEMENTAL) ChapterTriggers.fire(player, "fusion.elemental");
                boolean fusionOnly = !shard.sources().isEmpty()
                        && shard.sources().stream().allMatch(source -> source.type() == AttributeDefinition.SourceType.FUSION);
                if (fusionOnly) ChapterTriggers.fire(player, "shard.fusiononly");
            }
            default -> {
            }
        }
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onTreeGift(TreeGiftReceivedEvent event) {
        ChapterService.evaluate(event.getPlayer());
    }
}
