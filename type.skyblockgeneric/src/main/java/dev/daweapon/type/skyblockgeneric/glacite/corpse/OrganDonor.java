package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OrganDonor {
    public static final double RANGE = 20;
    private static final List<ItemType> ACCESSORIES = List.of(ItemType.ORGAN_DONOR_TALISMAN, ItemType.ORGAN_DONOR_RING, ItemType.ORGAN_DONOR_ARTIFACT);
    private static final Map<UUID, Long> NEXT_DING = new ConcurrentHashMap<>();
    private static boolean started;

    private OrganDonor() {
    }

    static synchronized void start() {
        if (started) return;
        started = true;
        MinecraftServer.getSchedulerManager().buildTask(OrganDonor::tick).repeat(TaskSchedule.tick(2)).schedule();
    }

    public static float pitch(double distance) {
        double ratio = (distance + 0.4) / RANGE;
        return (float) Math.clamp(2 - ratio * ratio, 0.5, 2.0);
    }

    public static double distance(float pitch) {
        return RANGE * Math.sqrt(2 - pitch) - 0.4;
    }

    public static long gapMillis(double distance) {
        if (distance < 4) return 550;
        if (distance < 12) return 600;
        return 800;
    }

    public static boolean wearing(SkyBlockPlayer player) {
        return ACCESSORIES.stream().anyMatch(player::hasTalisman);
    }

    private static void tick() {
        long now = System.currentTimeMillis();
        for (Map.Entry<Instance, ShaftCorpses> entry : ShaftCorpses.all()) {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getInstance() != entry.getKey() || player.isDead()) continue;
                ding(player, entry.getValue(), now);
            }
        }
    }

    private static void ding(SkyBlockPlayer player, ShaftCorpses shaft, long now) {
        if (NEXT_DING.getOrDefault(player.getUuid(), 0L) > now) return;
        double nearest = shaft.corpses().stream()
                .filter(corpse -> !shaft.isLooted(player.getUuid(), corpse))
                .mapToDouble(corpse -> corpse.position().distance(player.getPosition()))
                .min().orElse(Double.MAX_VALUE);
        if (nearest > RANGE || !wearing(player)) return;
        player.playSound(Sound.sound(Key.key("block.note_block.harp"), Sound.Source.PLAYER, 1f, pitch(nearest)), Sound.Emitter.self());
        NEXT_DING.put(player.getUuid(), now + gapMillis(nearest));
    }
}
