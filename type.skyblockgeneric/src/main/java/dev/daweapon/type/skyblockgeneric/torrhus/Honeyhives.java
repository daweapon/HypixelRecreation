package dev.daweapon.type.skyblockgeneric.torrhus;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.hologram.PlayerHolograms;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTF;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynResource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class Honeyhives {
    public static final Path POSITIONS = Path.of("./configuration/skyblock/torrhus/honeyhives.yml");
    public static final String CHAPTER_TRIGGER = "honeyhive.loot";
    public static final long COOLDOWN_MILLIS = 60L * 60 * 1000;
    public static final String HONEY_HAUL = "C64";
    public static final String HONEY_REFILL = "E49";
    private static final double HOLOGRAM_RANGE = 40;
    private static final double HOLOGRAM_DROP = -0.2;
    private static final double HOLOGRAM_SPACING = 0.4;

    private static final Map<UUID, Map<Long, Shown>> SHOWN = new ConcurrentHashMap<>();

    private Honeyhives() {
    }

    private record Shown(String text, PlayerHolograms.ExternalPlayerHologram hologram) {
    }

    public static boolean isHoneyhiveServer() {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_TORRHUS_CANYON;
    }

    public static long key(Point position) {
        return ((long) position.blockX() & 0x3FFFFFFL) << 38 | ((long) position.blockZ() & 0x3FFFFFFL) << 12 | (position.blockY() & 0xFFFL);
    }

    public static List<Pos> positions(Path file) {
        try (InputStream stream = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(stream);
            List<Pos> positions = new ArrayList<>();
            for (Object entry : (List<?>) root.get("hives")) {
                List<?> xyz = (List<?>) entry;
                positions.add(new Pos(((Number) xyz.get(0)).doubleValue(), ((Number) xyz.get(1)).doubleValue(), ((Number) xyz.get(2)).doubleValue()));
            }
            return positions;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + file, exception);
        }
    }

    public static long millisLeft(@Nullable Long lootedAt, long now) {
        return lootedAt == null ? 0 : Math.max(0, lootedAt + COOLDOWN_MILLIS - now);
    }

    public static long millisLeft(SkyBlockPlayer player, Point position, long now) {
        return millisLeft(HotfService.data(player).getHiveLootedAt().get(String.valueOf(key(position))), now);
    }

    private static void markLooted(SkyBlockPlayer player, Point position, long now) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        data.getHiveLootedAt().values().removeIf(lootedAt -> millisLeft(lootedAt, now) == 0);
        data.getHiveLootedAt().put(String.valueOf(key(position)), now);
        HotfService.save(player, data);
    }

    private static void clearLooted(SkyBlockPlayer player, Point position) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        data.getHiveLootedAt().remove(String.valueOf(key(position)));
        HotfService.save(player, data);
    }

    public static boolean handle(SkyBlockPlayer player, Point position, Block block) {
        if (!block.compare(Block.BEE_NEST) || !isHoneyhiveServer() || player.getSkyblockDataHandler() == null) return false;
        long left = millisLeft(player, position, System.currentTimeMillis());
        if (left > 0) {
            player.sendMessage("<c>This Honeyhive is empty! It will refill in <e>{}<c>.", StringUtility.formatTimeLeft(left));
            return true;
        }
        markLooted(player, position, System.currentTimeMillis());
        loot(player, position);
        return true;
    }

    private static void loot(SkyBlockPlayer player, Point position) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        player.sendMessage("<7><o>You stick your hand into the honeyhive and feel around...");
        int honeycomb = HoneyhiveLoot.honeycomb(random);
        double haul = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse(HONEY_HAUL));
        if (haul > 0 && random.nextDouble() * 100 < haul) honeycomb *= 2;
        HoneyhiveLoot.Kind kind = HoneyhiveLoot.pick(player.getUuid(), random);
        int coins = kind.rollCoins(random);
        final int amount = honeycomb;
        ScheduleUtility.delay(() -> deliver(player, position, kind, amount, coins), 20);
    }

    private static void deliver(SkyBlockPlayer player, Point position, HoneyhiveLoot.Kind kind, int honeycomb, int coins) {
        deliverItem(player, ItemType.HONEYCOMB, honeycomb);
        List<SkyBlockItem> drops = new ArrayList<>();
        drops.add(new SkyBlockItem(ItemType.HONEYCOMB, honeycomb));
        Text extra;
        switch (kind) {
            case SMALL_COINS, LARGE_COINS -> {
                player.addCoins(coins);
                extra = Text.of("<6>{} Coins", StringUtility.commaify(coins));
            }
            case ENCHANTED_HONEYCOMB -> {
                deliverItem(player, ItemType.ENCHANTED_HONEYCOMB, 1);
                drops.add(new SkyBlockItem(ItemType.ENCHANTED_HONEYCOMB, 1));
                extra = Text.of("<a>Enchanted Honeycomb");
            }
            default -> {
                AttributeDefinition definition = kind.definition();
                if (definition == null) {
                    extra = Text.of("<7>nothing else");
                    break;
                }
                player.getHuntingData().addShards(definition.id(), 1);
                HypixelEventHandler.callCustomEvent(new ShardCaughtEvent(player, definition, 1, ShardCaughtEvent.Source.HONEYHIVE, false));
                extra = Text.of("<color:{}>{} Shard", definition.rarity().itemRarity().getColor(), definition.shardName());
            }
        }
        player.sendMessage(Text.of("<6><l>HIVE! <f>You found <f>Honeycomb <8>x{} <7>and ", honeycomb).append(extra).append("<f>!"));
        HypixelEventHandler.callCustomEvent(new CustomBlockBreakEvent(player, Material.BEE_NEST, position, drops, false));
        for (int i = 0; i < honeycomb; i++) StarlynContests.recordGather(player, StarlynResource.HONEYCOMB);
        ChapterTriggers.fire(player, CHAPTER_TRIGGER);
        double refill = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse(HONEY_REFILL));
        if (refill > 0 && ThreadLocalRandom.current().nextDouble() * 100 < refill) {
            clearLooted(player, position);
            player.sendMessage("<e>The Honeyhive instantly refilled!");
        }
    }

    private static void deliverItem(SkyBlockPlayer player, ItemType item, int amount) {
        if (player.canInsertItemIntoSacks(item, amount)) {
            player.getSackItems().increase(item, amount);
            return;
        }
        player.addAndUpdateItem(item, amount);
    }

    public static String[] hologramText(long millisLeft) {
        if (millisLeft <= 0) return new String[]{"<6>Honeyhive", "<a>Click to loot!"};
        return new String[]{"<6>Honeyhive", "<7>Refills in <e>" + StringUtility.formatTimeLeft(millisLeft)};
    }

    public static void register(Scheduler scheduler) {
        List<Pos> hives = positions(POSITIONS);
        scheduler.scheduleTask(() -> {
            long now = System.currentTimeMillis();
            List<UUID> online = new ArrayList<>();
            SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
                online.add(player.getUuid());
                refreshHolograms(player, hives, now);
            });
            SHOWN.keySet().removeIf(uuid -> {
                if (online.contains(uuid)) return false;
                SHOWN.get(uuid).values().forEach(shown -> PlayerHolograms.removeExternalPlayerHologram(shown.hologram()));
                return true;
            });
        }, TaskSchedule.seconds(2), TaskSchedule.seconds(2), ExecutionType.TICK_END);
    }

    private static void refreshHolograms(SkyBlockPlayer player, List<Pos> hives, long now) {
        Map<Long, Shown> shown = SHOWN.computeIfAbsent(player.getUuid(), _ -> new HashMap<>());
        for (Pos hive : hives) {
            long key = key(hive);
            Shown current = shown.get(key);
            if (hive.distance(player.getPosition()) > HOLOGRAM_RANGE) {
                if (current != null) PlayerHolograms.removeExternalPlayerHologram(shown.remove(key).hologram());
                continue;
            }
            String[] text = hologramText(millisLeft(player, hive, now));
            String joined = String.join("\n", text);
            if (current != null && current.text().equals(joined)) continue;
            if (current != null) PlayerHolograms.removeExternalPlayerHologram(current.hologram());
            PlayerHolograms.ExternalPlayerHologram hologram = PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(hive.blockX() + 0.5, hive.blockY() - HOLOGRAM_DROP, hive.blockZ() + 0.5))
                    .player(player)
                    .text(text)
                    .spacing(HOLOGRAM_SPACING)
                    .build();
            PlayerHolograms.addExternalPlayerHologram(hologram);
            shown.put(key, new Shown(joined, hologram));
        }
    }
}
