package dev.daweapon.type.skyblockgeneric.hunting.floordrop;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.entity.TextDisplayEntity;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.hotf.HotfCurrency;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
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
    private static final Map<UUID, List<Drop>> ACTIVE = new HashMap<>();
    private static FloorDropTable table;
    private static boolean started;

    private static final class Drop {
        private final List<Entity> entities = new ArrayList<>();
        private final Pos position;
        private final long expiresAt;
        private boolean taken;

        private Drop(Pos position, long expiresAt) {
            this.position = position;
            this.expiresAt = expiresAt;
        }

        private void remove() {
            entities.forEach(Entity::remove);
            entities.clear();
        }
    }

    private FloorDrops() {
    }

    @SuppressWarnings("unchecked")
    public static synchronized void start() {
        if (started || HypixelConst.getTypeLoader() == null) return;
        if (HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_TORRHUS_CANYON) return;
        try {
            table = FloorDropTable.parse((Map<String, Object>) YamlFileUtils.loadYaml(FILE).get("torrhus_canyon"));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        started = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            tick();
            return TaskSchedule.seconds(table.spawnSeconds());
        }, ExecutionType.TICK_START);
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
        UUID owner = player.getUuid();
        LivingEntity marker = new LivingEntity(EntityType.ITEM_DISPLAY);
        marker.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(ItemStack.of(Material.STRING));
            meta.setScale(new Vec(0.6, 0.6, 0.6));
            meta.setHasNoGravity(true);
        });
        TextDisplayEntity label = new TextDisplayEntity(Text.of("<e>Floor Drop"), meta -> {
        });
        InteractionEntity click = new InteractionEntity(1, 1, (clicker, event) -> collect((SkyBlockPlayer) clicker, drop));
        marker.setInstance(player.getInstance(), position.add(0, 0.2, 0));
        label.setInstance(player.getInstance(), position.add(0, 0.8, 0));
        click.setInstance(player.getInstance(), position.add(0, 0.5, 0));
        for (Entity entity : List.of(marker, label, click)) {
            entity.updateViewableRule(viewer -> viewer.getUuid().equals(owner));
            drop.entities.add(entity);
        }
        return drop;
    }

    private static void collect(SkyBlockPlayer player, Drop drop) {
        if (drop.taken) return;
        drop.taken = true;
        drop.remove();
        FloorDropTable.Reward reward = LootRoll.pick(player.getUuid(), table.rewards(), FloorDrops::itemOf, FloorDropTable.Reward::weight,
                ThreadLocalRandom.current());
        award(player, reward);
        ChapterTriggers.fire(player, PICKUP_TRIGGER);
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
                player.sendMessage("<e>Floor Drop<7>: <6>+{} Desert Whispers", reward.amount());
            }
            case ITEM -> {
                ItemType type = ItemType.get(reward.id());
                if (type == null) return;
                int amount = fortuneAmount(reward.amount(), player.getStatistics().allStatistics().getOverall(ItemStatistic.FORAGING_FORTUNE));
                player.addAndUpdateItem(type, amount);
                player.sendMessage("<e>Floor Drop<7>: <a>+{}x <f>{}", amount, dev.daweapon.commons.StringUtility.toNormalCase(type.getDisplayName()));
            }
            case SHARD -> AttributeRegistry.findByShard(reward.id()).ifPresent(definition -> awardShard(player, definition));
        }
    }

    private static void awardShard(SkyBlockPlayer player, AttributeDefinition definition) {
        int amount = AttributeEffectService.fortunateAmount(player, definition);
        player.getHuntingData().addShards(definition.id(), amount);
        dev.daweapon.type.generic.event.HypixelEventHandler.callCustomEvent(
                new ShardCaughtEvent(player, definition, amount, ShardCaughtEvent.Source.TREE_GIFT, false));
        player.sendMessage("<e>Floor Drop<7>: <a>+{}x <f>{} Shard", amount, definition.shardName());
    }

    public static int fortuneAmount(int base, double fortune) {
        double scaled = base * (1 + fortune / 100D);
        int whole = (int) scaled;
        return ThreadLocalRandom.current().nextDouble() < scaled - whole ? whole + 1 : whole;
    }
}
