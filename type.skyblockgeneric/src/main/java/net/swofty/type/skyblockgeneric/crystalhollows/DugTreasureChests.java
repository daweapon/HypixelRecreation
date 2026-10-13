package net.swofty.type.skyblockgeneric.crystalhollows;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.hotm.HotmPerk;
import net.swofty.type.skyblockgeneric.hotm.HotmRegistry;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.commission.CommissionProgress;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class DugTreasureChests {
    public static final String GREAT_EXPLORER = "great_explorer";
    public static final int BASE_LOCKS = 5;
    public static final double HIGH_TIER_PERCENT = 5;
    public static final int LIFETIME_SECONDS = 60;

    private static final Map<String, DugChest> CHESTS = new ConcurrentHashMap<>();

    private DugTreasureChests() {
    }

    private static final class DugChest {
        private final UUID owner;
        private final Instance instance;
        private final Point point;
        private final Block previous;
        private final TreasurePool pool;
        private int locks;

        private DugChest(UUID owner, Instance instance, Point point, Block previous, TreasurePool pool, int locks) {
            this.owner = owner;
            this.instance = instance;
            this.point = point;
            this.previous = previous;
            this.pool = pool;
            this.locks = locks;
        }
    }

    public static double chancePercent(SkyBlockPlayer player) {
        double chance = HardStone.TREASURE_CHANCE_PERCENT + HotmService.perkValue(player, GREAT_EXPLORER) / 100;
        chance += PetAbilities.value(player, ItemType.SCATHA_PET, "Burrowing", 0) / 100;
        return chance;
    }

    public static int locks(SkyBlockPlayer player) {
        HotmPerk perk = HotmRegistry.get().perk(GREAT_EXPLORER);
        int removed = perk == null ? 0 : (int) perk.value("second", HotmService.data(player).activeLevel(GREAT_EXPLORER));
        return Math.max(0, BASE_LOCKS - removed);
    }

    public static void onHardStoneMined(SkyBlockPlayer player, Point point) {
        if (!LootRoll.roll(player.getUuid(), null, chancePercent(player))) return;
        Instance instance = player.getInstance();
        if (instance == null) return;
        TreasurePool pool = LootRoll.roll(player.getUuid(), null, HIGH_TIER_PERCENT) ? TreasurePool.HIGH : TreasurePool.LOW;
        MinecraftServer.getSchedulerManager().scheduleTask(() -> spawn(player, instance, point, pool), TaskSchedule.tick(2), TaskSchedule.stop());
    }

    private static void spawn(SkyBlockPlayer player, Instance instance, Point point, TreasurePool pool) {
        String key = key(point);
        if (CHESTS.containsKey(key)) return;
        DugChest chest = new DugChest(player.getUuid(), instance, point, instance.getBlock(point), pool, locks(player));
        CHESTS.put(key, chest);
        instance.setBlock(point, Block.CHEST);
        player.sendMessage(Text.of("<6>You uncovered a treasure chest!"));
        MinecraftServer.getSchedulerManager().scheduleTask(() -> expire(key, chest), TaskSchedule.seconds(LIFETIME_SECONDS), TaskSchedule.stop());
    }

    private static void expire(String key, DugChest chest) {
        if (CHESTS.remove(key, chest)) chest.instance.setBlock(chest.point, chest.previous);
    }

    public static boolean isDugChest(Point point) {
        return CHESTS.containsKey(key(point));
    }

    public static void interact(SkyBlockPlayer player, Point point) {
        String key = key(point);
        DugChest chest = CHESTS.get(key);
        if (chest == null) return;
        if (!chest.owner.equals(player.getUuid())) {
            player.sendMessage(Text.of("<c>This treasure chest isn't yours!"));
            return;
        }
        if (chest.locks > 0) {
            chest.locks--;
            player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1, 1 + 0.2f * (BASE_LOCKS - chest.locks)));
            if (chest.locks > 0) {
                player.sendActionBar(Text.of("<e>{} locks left", chest.locks).asComponent());
                return;
            }
        }
        if (!CHESTS.remove(key, chest)) return;
        chest.instance.setBlock(chest.point, chest.previous);
        player.sendMessage(Text.of("<a><l>CHEST LOCKPICKED"));
        TreasureLoot.open(player, chest.pool, ThreadLocalRandom.current(), ThreadLocalRandom.current());
        CommissionProgress.recordChestOpened(player);
        var data = CrystalHollowsAccess.data(player);
        data.addCounter("treasure_chests_opened", 1);
        CrystalHollowsAccess.save(player, data);
    }

    private static String key(Point point) {
        return point.blockX() + "," + point.blockY() + "," + point.blockZ();
    }
}
