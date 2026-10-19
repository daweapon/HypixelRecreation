package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.RabbitHitman;
import dev.daweapon.type.skyblockgeneric.rabbits.RabbitShardBonuses;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class HoppityHitmanService {
    private static final int PERIOD_SECONDS = 30;

    private HoppityHitmanService() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                try {
                    collectFor(player);
                } catch (Exception ignored) {
                }
            }
        }).repeat(TaskSchedule.seconds(PERIOD_SECONDS)).delay(TaskSchedule.seconds(PERIOD_SECONDS)).schedule();
    }

    public static void collectFor(SkyBlockPlayer player) {
        long elapsed = SkyBlockCalendar.getElapsed();
        if (!HuntEggSchedule.isHuntActive(elapsed)) return;
        long now = System.currentTimeMillis();
        int year = HuntEggSchedule.yearOf(elapsed);
        List<Integer> active = HuntEggSchedule.activeSpawnNumbers(elapsed);
        int windowStart = active.isEmpty() ? 0 : active.getFirst();
        double reduction = RabbitShardBonuses.hitmanCooldownPercent(player);
        double finderSeconds = RabbitShardBonuses.eggFinderSeconds(player);
        EggLocationCatalogue catalogue = EggLocationCatalogue.get();
        String marker = RabbitHitman.markerOf(year, Math.max(0, windowStart - 1));

        ChocolateFactoryData current = ChocolateFactoryHelper.getData(player);
        boolean markerDue = !marker.equals(current.getHitmanLastCheckedSpawn());
        boolean finderDue = finderSeconds > 0 && RabbitHitman.purchasedSlots(current) > 0
                && (current.getHitmanLastEggFinder() == 0 || now - current.getHitmanLastEggFinder() >= finderSeconds * 1000);
        if (!markerDue && !finderDue) return;

        ChocolateFactoryHelper.mutateData(player, data -> {
            if (markerDue) {
                int free = RabbitHitman.freeSlots(data, now, reduction);
                int previous = RabbitHitman.lastChecked(data, year);
                for (int number : RabbitHitman.missedSpawns(year, windowStart, data.getClaimedSpawns(), previous, free)) {
                    EggLocation location = RabbitHitman.pickLocation(player.getUuid(), HuntEggSchedule.spawnId(year, number), year, number, catalogue);
                    if (location != null) RabbitHitman.store(data, location.key());
                }
                data.setHitmanLastCheckedSpawn(marker);
            }
            if (finderDue) grantFinderEggs(player, data, now, reduction, finderSeconds, year, windowStart, catalogue);
        });
    }

    private static void grantFinderEggs(SkyBlockPlayer player, ChocolateFactoryData data, long now, double reduction,
                                        double finderSeconds, int year, int spawnNumber, EggLocationCatalogue catalogue) {
        long last = data.getHitmanLastEggFinder();
        if (last == 0) {
            data.setHitmanLastEggFinder(now);
            return;
        }
        long intervalMs = (long) (finderSeconds * 1000);
        long due = (now - last) / intervalMs;
        int granted = 0;
        for (long i = 0; i < due && RabbitHitman.freeSlots(data, now, reduction) > 0; i++) {
            EggLocation location = RabbitHitman.pickLocation(player.getUuid(), "finder:" + (last + (i + 1) * intervalMs), year, spawnNumber, catalogue);
            if (location == null) break;
            RabbitHitman.store(data, location.key());
            granted++;
        }
        data.setHitmanLastEggFinder(granted < due ? now : last + due * intervalMs);
    }

    public static boolean open(SkyBlockPlayer player, int eggIndex) {
        long now = System.currentTimeMillis();
        double reduction = RabbitShardBonuses.hitmanCooldownPercent(player);
        String[] opened = {null};
        ChocolateFactoryHelper.mutateData(player, data -> {
            if (eggIndex < 0 || eggIndex >= data.getHitmanEggs().size()) return;
            if (!RabbitHitman.canOpen(data, now, reduction)) return;
            opened[0] = data.getHitmanEggs().remove(eggIndex);
            RabbitHitman.startCooldown(data, now, reduction);
            data.setStrayDueAt(now + HoppityEggService.STRAY_GUARANTEE_MS);
        });
        if (opened[0] == null) return false;
        player.sendMessage("<d><l>HOPPITY'S HUNT</l> <d>You found a <c>Hitman Egg<d>!");
        HoppityEggService.open(player, RabbitHitman.locationOf(opened[0], EggLocationCatalogue.get()), null, ThreadLocalRandom.current());
        return true;
    }

    public static boolean buyNextSlot(SkyBlockPlayer player) {
        ChocolateFactoryData current = ChocolateFactoryHelper.getData(player);
        if (!RabbitHitman.canBuyNextSlot(current)) return false;
        long price = RabbitHitman.slotPrice(RabbitHitman.purchasedSlots(current));
        if (player.getCoins() < price) return false;
        player.removeCoins(price);
        ChocolateFactoryHelper.mutateData(player, data -> data.setHitmanSlots(RabbitHitman.purchasedSlots(data) + 1));
        return true;
    }
}
