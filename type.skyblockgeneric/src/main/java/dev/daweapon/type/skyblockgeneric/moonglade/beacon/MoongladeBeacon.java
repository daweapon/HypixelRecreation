package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.util.concurrent.atomic.AtomicBoolean;

public final class MoongladeBeacon {
    public static final String LEVEL_TRIGGER = "beacon.signal_level";
    public static final String TUNE_TRIGGER = "beacon.tune";
    public static final String UPGRADE_TRIGGER = "beacon.upgrade";
    public static final String BEACONMITE_TRIGGER = "beaconmite.find";
    public static final AttributeId BEACON_ZEALOT = AttributeId.parse("R7");
    public static final String BEACONMITE_SHARD = "Beaconmite";
    public static final int SKYBLOCK_XP_PER_LEVEL = 2;

    private static final BeaconStacks STACKS = new BeaconStacks();
    private static final AtomicBoolean STARTED = new AtomicBoolean();
    private static volatile int lastCount;

    private MoongladeBeacon() {
    }

    public static boolean onBeaconServer() {
        return BeaconSite.current().isPresent();
    }

    public static void start() {
        if (!STARTED.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            int count = STACKS.active(System.currentTimeMillis());
            if (count == lastCount) return;
            lastCount = count;
            refreshAll();
        }, TaskSchedule.seconds(1), TaskSchedule.seconds(1));
    }

    public static BeaconSite site() {
        return BeaconSite.current().orElse(BeaconSite.MOONGLADE);
    }

    public static int level(SkyBlockPlayer player) {
        return level(player, site());
    }

    public static int level(SkyBlockPlayer player, BeaconSite site) {
        if (player.getSkyblockDataHandler() == null) return 0;
        return (int) Math.min(BeaconRules.MAX_LEVEL, ChapterService.data(player).trigger(site.levelTrigger()));
    }

    public static int activeStacks() {
        return STACKS.active(System.currentTimeMillis());
    }

    public static double zealotPercent(SkyBlockPlayer player) {
        return AttributeEffectService.value(player.getHuntingData(), BEACON_ZEALOT);
    }

    public static double fortune(SkyBlockPlayer player) {
        if (!onBeaconServer() || player.getSkyblockDataHandler() == null) return 0;
        return BeaconRules.fortune(activeStacks(), level(player), zealotPercent(player));
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        double fortune = fortune(player);
        if (fortune <= 0) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic statistic : site().statistics()) builder.withBase(statistic, fortune);
        return builder.build();
    }

    public static void completeTuning(SkyBlockPlayer player) {
        STACKS.add(System.currentTimeMillis() + BeaconRules.STACK_MILLIS);
        SkyBlockGenericLoader.forEachLoadedPlayer(other -> other.sendMessage(
                "<9>{} <8>» <b>{} <f>tuned the frequency! Everyone on the island gains <b>+1 Stack <f>for <a>20min<f>.",
                site().displayName(), player.getUsername()));
        refreshAll();
        ChapterTriggers.fire(player, TUNE_TRIGGER);
        rollBeaconmite(player);
    }

    public static boolean completeUpgrade(SkyBlockPlayer player) {
        int level = level(player);
        if (level >= BeaconRules.MAX_LEVEL) return false;
        if (player.takeItem(ItemType.SIGNAL_ENHANCER, 1) == null) {
            player.sendMessage("<c>You need a Signal Enhancer to do that!");
            return false;
        }
        ChapterTriggers.atLeast(player, site().levelTrigger(), level + 1L);
        ChapterTriggers.fire(player, UPGRADE_TRIGGER);
        syncSkyBlockXp(player);
        player.getStatistics().invalidateStatistics();
        player.sendMessage("<9>{} <8>» <a>Signal strength improved to <b>{}<a>! <7>(Stack limit <a>{}<7>, <6>+{}☘ <7>per Stack)",
                site().displayName(), level + 1, BeaconRules.stackLimit(level + 1), BeaconRules.format(BeaconRules.fortunePerStack(level + 1)));
        rollBeaconmite(player);
        return true;
    }

    public static void syncSkyBlockXp(SkyBlockPlayer player) {
        if (!SkyBlockXPSource.BEACON_TIERS.isObtainable() || player.getSkyblockDataHandler() == null) return;
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.BEACON_TIERS, (double) SKYBLOCK_XP_PER_LEVEL * level(player));
    }

    public static void resetStacks() {
        STACKS.clear();
        lastCount = 0;
        refreshAll();
    }

    private static void refreshAll() {
        SkyBlockGenericLoader.forEachLoadedPlayer(player -> player.getStatistics().invalidateStatistics());
    }

    private static void rollBeaconmite(SkyBlockPlayer player) {
        AttributeDefinition definition = AttributeRegistry.findByShard(BEACONMITE_SHARD).orElse(null);
        if (definition == null) return;
        if (!LootRoll.roll(player.getUuid(), AttributeShardComponent.itemType(definition),
                BeaconRules.beaconmitePercent(level(player)))) return;
        player.getHuntingData().addShards(definition.id(), 1);
        HypixelEventHandler.callCustomEvent(new ShardCaughtEvent(player, definition, 1, ShardCaughtEvent.Source.BEACON, false));
        ChapterTriggers.fire(player, BEACONMITE_TRIGGER);
        player.sendMessage(Text.of("<9>" + site().displayName() + " <8>» <f>A <9>Beaconmite Shard <f>was drawn out of the signal!"));
    }
}
