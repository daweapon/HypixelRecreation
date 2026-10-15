package net.swofty.type.skyblockgeneric.crystalhollows;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.hotm.HotmPowder;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class MetalDetector {
    public static final String HANDLER_ID = "METAL_DETECTOR_DIG";
    public static final double DETECTION_RANGE = 30;
    public static final double DIG_RADIUS = 3;
    private static final int TICK_INTERVAL = 2;

    public interface DigSite {
        List<Point> spots();

        boolean delivered(UUID player, ItemType scavenged);
    }

    public record Reading(int index, double distance) {
    }

    private static final DigSite NO_SITE = new DigSite() {
        @Override
        public List<Point> spots() {
            return List.of();
        }

        @Override
        public boolean delivered(UUID player, ItemType scavenged) {
            return false;
        }
    };

    private static final Map<UUID, Set<Integer>> CLEARED = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> BEEP_COUNTDOWN = new ConcurrentHashMap<>();
    private static volatile DigSite site = NO_SITE;
    private static boolean started;

    private MetalDetector() {
    }

    public static void install(DigSite digSite) {
        site = digSite;
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        MinecraftServer.getSchedulerManager().buildTask(MetalDetector::tick).repeat(TaskSchedule.tick(TICK_INTERVAL)).schedule();
    }

    public static void registerInteractables() {
        InteractableRegistry.register(HANDLER_ID, InteractableItemConfig.builder()
                .rightClickHandler(MetalDetector::dig)
                .build());
    }

    public static Reading nearest(List<? extends Point> spots, Set<Integer> cleared, Point from, double range) {
        Reading best = null;
        for (int i = 0; i < spots.size(); i++) {
            if (cleared.contains(i)) continue;
            double distance = from.distance(spots.get(i));
            if (distance > range || (best != null && distance >= best.distance())) continue;
            best = new Reading(i, distance);
        }
        return best;
    }

    public static String distanceLabel(double distance) {
        return String.format(java.util.Locale.ROOT, "%.1f meters away", distance);
    }

    public static int beepIntervalTicks(double distance) {
        return (int) Math.max(4, Math.min(40, Math.round(distance * 1.2)));
    }

    public static float beepPitch(double distance) {
        double closeness = 1 - Math.min(1, Math.max(0, distance / DETECTION_RANGE));
        return (float) (0.5 + 1.5 * closeness);
    }

    public static boolean inDivan(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        return region != null && region.getType() == RegionType.MINES_OF_DIVAN;
    }

    private static boolean holdingDetector(SkyBlockPlayer player) {
        if (player.getItemInMainHand().isAir()) return false;
        return new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType() == ItemType.DWARVEN_METAL_DETECTOR;
    }

    private static void tick() {
        List<Point> spots = site.spots();
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (spots.isEmpty() || !holdingDetector(player)) {
                BEEP_COUNTDOWN.remove(player.getUuid());
                continue;
            }
            Reading reading = nearest(spots, CLEARED.getOrDefault(player.getUuid(), Set.of()), player.getPosition(), DETECTION_RANGE);
            if (reading == null) {
                BEEP_COUNTDOWN.remove(player.getUuid());
                continue;
            }
            SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.PROTECTION,
                    Text.of("<e><l>{}", distanceLabel(reading.distance())), 10, 2);
            int left = BEEP_COUNTDOWN.merge(player.getUuid(), -TICK_INTERVAL, Integer::sum);
            if (left > 0) continue;
            BEEP_COUNTDOWN.put(player.getUuid(), beepIntervalTicks(reading.distance()));
            player.playSound(Sound.sound(Key.key("block.note_block.bit"), Sound.Source.PLAYER, 1, beepPitch(reading.distance())));
        }
    }

    private static void dig(SkyBlockPlayer player, SkyBlockItem item) {
        List<Point> spots = site.spots();
        Set<Integer> cleared = CLEARED.computeIfAbsent(player.getUuid(), _ -> ConcurrentHashMap.newKeySet());
        Reading reading = nearest(spots, cleared, player.getPosition(), DIG_RADIUS);
        if (reading == null) {
            player.sendMessage(Text.of("<7>You don't find anything here. Follow the beeps of the Metal Detector!"));
            return;
        }
        cleared.add(reading.index());
        if (cleared.size() >= spots.size()) cleared.clear();
        award(player);
    }

    private static void award(SkyBlockPlayer player) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<ItemType> missing = MetalDetectorLoot.missing(scavenged ->
                player.getAmountAnywhere(scavenged) > 0 || site.delivered(player.getUuid(), scavenged));
        MetalDetectorLoot.Entry entry = MetalDetectorLoot.roll(player.getUuid(), MetalDetectorLoot.table(missing), random);
        int amount = MetalDetectorLoot.amount(entry, random);
        player.playSound(Sound.sound(Key.key("block.gravel.break"), Sound.Source.PLAYER, 1, 1));
        if (entry.powder()) {
            long gained = HotmService.gainPowder(player, HotmPowder.GEMSTONE, amount);
            player.sendMessage(Text.of("<6><l>TREASURE! <7>You found <d>{} Gemstone Powder<7>!", String.format("%,d", gained)));
            return;
        }
        SkyBlockItem reward = new SkyBlockItem(entry.item(), amount);
        player.addAndUpdateItem(reward);
        player.sendMessage(Text.of("<6><l>TREASURE! <7>You found <f>{}x {}<7>!", amount, reward.getDisplayName()));
    }
}
