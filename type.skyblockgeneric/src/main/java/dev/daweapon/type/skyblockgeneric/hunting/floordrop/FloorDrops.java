package dev.daweapon.type.skyblockgeneric.hunting.floordrop;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.hotf.HotfCurrency;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class FloorDrops {
    public static final String PICKUP_TRIGGER = "floordrop.pickup";
    private static final File FILE = new File("./configuration/skyblock/hunting/floor_drops.yml");
    private static final int ATTEMPTS = 10;
    public static final String GROUND_TRIGGER = "ground.find";
    private static final Block SHINING_BLOCK = Block.BROWN_MUSHROOM_BLOCK;
    private static final Map<UUID, List<Drop>> ACTIVE = new HashMap<>();
    private static FloorDropTable table;
    private static boolean started;

    private static final class Drop {
        private final List<Entity> entities = new ArrayList<>();
        private final Pos position;
        private SkyBlockPlayer owner;
        private final long expiresAt;
        private boolean taken;

        private Drop(Pos position, long expiresAt) {
            this.position = position;
            this.expiresAt = expiresAt;
        }

        private void remove() {
            entities.forEach(Entity::remove);
            entities.clear();
            if (owner != null && owner.getInstance() != null)
                owner.sendPacket(new BlockChangePacket(position.sub(0, 1, 0), owner.getInstance().getBlock(position.sub(0, 1, 0))));
        }
    }

    private FloorDrops() {
    }

    @SuppressWarnings("unchecked")
    public static synchronized void start() {
        if (started || HypixelConst.getTypeLoader() == null) return;
        String zone = switch (HypixelConst.getTypeLoader().getType()) {
            case SKYBLOCK_TORRHUS_CANYON -> "torrhus_canyon";
            case SKYBLOCK_GALATEA -> "moonglade_marsh";
            default -> null;
        };
        if (zone == null) return;
        try {
            table = FloorDropTable.parse((Map<String, Object>) YamlFileUtils.loadYaml(FILE).get(zone));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        started = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            tick();
            return TaskSchedule.seconds(table.spawnSeconds());
        }, ExecutionType.TICK_START);
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            sparkle();
            return TaskSchedule.tick(8);
        }, ExecutionType.TICK_END);
    }

    private static void sparkle() {
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            List<Drop> drops = ACTIVE.get(player.getUuid());
            if (drops == null) continue;
            for (Drop drop : drops) {
                if (drop.taken || drop.position.distance(player.getPosition()) > 48) continue;
                Pos top = drop.position.add(0, 0.15, 0);
                player.sendPacket(new ParticlePacket(Particle.DUST.withColor(new Color(255, 255, 255)), true, true, top.x(), top.y(), top.z(), 0.3f, 0.03f, 0.3f, 0f, 3));
                player.sendPacket(new ParticlePacket(Particle.HAPPY_VILLAGER, true, true, top.x(), top.y() + 0.3, top.z(), 0.3f, 0.1f, 0.3f, 0f, 1));
            }
        }
    }

    private static void tick() {
        List<SkyBlockPlayer> players = SkyBlockGenericLoader.getLoadedPlayers();
        ACTIVE.keySet().removeIf(uuid -> {
            if (players.stream().anyMatch(player -> player.getUuid().equals(uuid))) return false;
            ACTIVE.get(uuid).forEach(Drop::remove);
            return true;
        });
        for (SkyBlockPlayer player : players) {
            if (player.getInstance() == null) continue;
            List<Drop> drops = ACTIVE.computeIfAbsent(player.getUuid(), uuid -> new ArrayList<>());
            for (Iterator<Drop> iterator = drops.iterator(); iterator.hasNext(); ) {
                Drop drop = iterator.next();
                boolean far = drop.position.distance(player.getPosition()) > table.maxDistance() * 2;
                if (drop.taken || far || System.currentTimeMillis() > drop.expiresAt) {
                    drop.remove();
                    iterator.remove();
                }
            }
            if (drops.size() < table.maxActive()) spawn(player, drops);
        }
    }

    private static void spawn(SkyBlockPlayer player, List<Drop> drops) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Instance instance = player.getInstance();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            double angle = random.nextDouble(Math.PI * 2);
            double distance = random.nextDouble(table.minDistance(), table.maxDistance());
            int x = (int) Math.floor(player.getPosition().x() + Math.cos(angle) * distance);
            int z = (int) Math.floor(player.getPosition().z() + Math.sin(angle) * distance);
            if (!instance.isChunkLoaded(x >> 4, z >> 4)) continue;
            Pos grass = grass(instance, x, z, (int) player.getPosition().y() + 24);
            if (grass == null || drops.stream().anyMatch(drop -> drop.position.distance(grass) < 4)) continue;
            drops.add(create(player, grass));
            return;
        }
    }

    static Pos grass(Instance instance, int x, int z, int startY) {
        for (int y = startY; y > startY - 64; y--) {
            Block block = instance.getBlock(x, y, z);
            if (block.isAir() || block.name().contains("leaves")) continue;
            if (block.compare(Block.GRASS_BLOCK) && instance.getBlock(x, y + 1, z).isAir()) return new Pos(x + 0.5, y + 1, z + 0.5);
            return null;
        }
        return null;
    }

    private static Drop create(SkyBlockPlayer player, Pos position) {
        Drop drop = new Drop(position, System.currentTimeMillis() + table.lifetimeSeconds() * 1000L);
        drop.owner = player;
        UUID owner = player.getUuid();
        InteractionEntity click = new InteractionEntity(1, 1, (clicker, event) -> collect((SkyBlockPlayer) clicker, drop));
        click.setInstance(player.getInstance(), position.add(0, 0.5, 0));
        click.updateViewableRule(viewer -> viewer.getUuid().equals(owner));
        drop.entities.add(click);
        player.sendPacket(new BlockChangePacket(position.sub(0, 1, 0), SHINING_BLOCK));
        return drop;
    }

    private static void collect(SkyBlockPlayer player, Drop drop) {
        if (drop.taken) return;
        drop.taken = true;
        drop.remove();
        FloorDropTable.Reward reward = LootRoll.pick(player.getUuid(), table.rewards(), FloorDrops::itemOf, FloorDropTable.Reward::weight,
                ThreadLocalRandom.current());
        award(player, reward);
        player.playSound(Sound.sound(Key.key("entity.item.pickup"), Sound.Source.PLAYER, 1f, 1.49f));
        ChapterTriggers.fire(player, PICKUP_TRIGGER);
        ChapterTriggers.fire(player, GROUND_TRIGGER);
    }

    private static ItemType itemOf(FloorDropTable.Reward reward) {
        return switch (reward.kind()) {
            case ITEM -> ItemType.get(reward.id());
            case SHARD -> AttributeRegistry.findByShard(reward.id()).map(AttributeShardComponent::itemType).orElse(null);
            case WHISPERS -> null;
        };
    }

    private static void award(SkyBlockPlayer player, FloorDropTable.Reward reward) {
        switch (reward.kind()) {
            case WHISPERS -> {
                HotfService.addWhispers(player, HotfCurrency.DESERT, reward.amount());
                found(player, "Desert Whispers", reward.amount());
            }
            case ITEM -> {
                ItemType type = ItemType.get(reward.id());
                if (type == null) return;
                int amount = fortuneAmount(reward.amount(), player.getStatistics().allStatistics().getOverall(ItemStatistic.FORAGING_FORTUNE));
                player.addAndUpdateItem(type, amount);
                found(player, dev.daweapon.commons.StringUtility.toNormalCase(type.getDisplayName()), amount);
            }
            case SHARD -> AttributeRegistry.findByShard(reward.id()).ifPresent(definition -> awardShard(player, definition));
        }
    }

    private static void awardShard(SkyBlockPlayer player, AttributeDefinition definition) {
        int amount = AttributeEffectService.fortunateAmount(player, definition);
        player.getHuntingData().addShards(definition.id(), amount);
        dev.daweapon.type.generic.event.HypixelEventHandler.callCustomEvent(
                new ShardCaughtEvent(player, definition, amount, ShardCaughtEvent.Source.TREE_GIFT, false));
        found(player, definition.shardName() + " Shard", amount);
    }

    private static void found(SkyBlockPlayer player, String name, int amount) {
        player.sendMessage("<6><l>FLOOR DROP! <7>You found <f>" + name + " <8>x" + amount + "<7> on the ground!");
    }

    public static int fortuneAmount(int base, double fortune) {
        double scaled = base * (1 + fortune / 100D);
        int whole = (int) scaled;
        return ThreadLocalRandom.current().nextDouble() < scaled - whole ? whole + 1 : whole;
    }
}
