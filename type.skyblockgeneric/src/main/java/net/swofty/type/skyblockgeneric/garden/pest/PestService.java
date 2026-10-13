package net.swofty.type.skyblockgeneric.garden.pest;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.title.Title;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenProgress;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.item.equipment.EquipmentBonuses;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

public final class PestService {
    private PestService() {
    }

    public static boolean unlocked(GardenProfile profile) {
        return GardenProgress.level(profile) >= PestRules.MIN_GARDEN_LEVEL;
    }

    public static int bonusChance(SkyBlockPlayer player, GardenProfile profile) {
        double stat = player.getStatistics().allStatistics().getOverall(ItemStatistic.BONUS_PEST_CHANCE);
        return (int) Math.floor(stat) + profile.pests().getPermanentBonusChance();
    }

    public static int fortuneLoss(SkyBlockPlayer player, GardenProfile profile) {
        return PestRules.fortuneLossPercent(profile.pests().aliveCount(), bonusChance(player, profile));
    }

    public static int cooldownReduction(SkyBlockPlayer player) {
        long pieces = EquipmentBonuses.wornItems(player).keySet().stream().filter(id -> id.startsWith("PESTHUNTERS_")).count();
        return (int) pieces * PestRules.COOLDOWN_REDUCTION_PER_PESTHUNTER_PIECE;
    }

    public static double fortuneBoost(GardenProfile profile, long now) {
        PestData data = profile.pests();
        return data.getBoostUntil() > now ? data.getBoostFortune() : 0;
    }

    public static void onHarvest(SkyBlockPlayer player, int plot) {
        GardenProfile profile = Gardens.profile(player);
        if (!unlocked(profile)) return;
        PestData data = profile.pests();
        long now = System.currentTimeMillis();
        if (now < data.getCooldownUntil() || data.aliveCount() >= PestData.MAX_ALIVE) return;
        PestData.Spray spray = data.sprayOn(plot, now);
        if (ThreadLocalRandom.current().nextDouble() * 100 >= PestRules.spawnChancePercent(spray != null)) return;
        int chance = bonusChance(player, profile) + (spray != null ? PestRules.SPRAY_BONUS_CHANCE : 0);
        int count = PestRules.pestsPerSpawn(chance, ThreadLocalRandom.current().nextDouble());
        spawn(player, farmedPlotOrNeighbour(profile, plot), count, null);
    }

    public static List<PestData.Alive> spawn(SkyBlockPlayer player, int plot, int count, @Nullable String forcedType) {
        GardenProfile profile = Gardens.profile(player);
        PestData data = profile.pests();
        long now = System.currentTimeMillis();
        int before = fortuneLoss(player, profile);
        List<PestData.Alive> spawned = new ArrayList<>();
        for (int i = 0; i < count && data.aliveCount() < PestData.MAX_ALIVE; i++) {
            PestCatalogue.Pest type = forcedType != null ? PestCatalogue.get().pest(forcedType) : pickType(profile, plot, now);
            if (type == null) continue;
            PestData.Alive alive = PestData.Alive.create(type.id(), plot);
            data.getAlive().add(alive);
            spawned.add(alive);
        }
        if (spawned.isEmpty()) return spawned;
        boolean repelled = data.getRepellentUntil() > now;
        data.setCooldownUntil(now + PestRules.cooldownMillis(repelled && !data.isRepellentMax(), repelled && data.isRepellentMax(),
                cooldownReduction(player)));
        Gardens.save(player, profile);
        Instance instance = player.getInstance();
        if (instance != null) PestWorld.reconcile(instance, data);
        announce(player, plot, spawned.size(), before, fortuneLoss(player, profile));
        return spawned;
    }

    private static void announce(SkyBlockPlayer player, int plot, int count, int lossBefore, int lossNow) {
        String lead = count == 1 ? "<6><l>GROSS!</l> <7>A <2>Pest <7>has appeared in <a>Plot <7>- <b>{}<7>!"
                : count == 2 ? "<6><l>EWW!</l> <7>{} <2>Pests <7>have spawned in <a>Plot <7>- <b>{}<7>!"
                : "<6><l>YUCK!</l> <7>{} <2>Pests <7>have spawned in <a>Plot <7>- <b>{}<7>!";
        player.sendMessage(count == 1 ? Text.of(lead, plot) : Text.of(lead, count, plot));
        player.sendMessage(Text.of("<e><l>CLICK HERE</l> <e>to teleport to the plot!").asComponent()
                .clickEvent(ClickEvent.runCommand("/plottp " + plot))
                .hoverEvent(HoverEvent.showText(Text.of("<e>Click to teleport to Plot - {}!", plot).asComponent())));
        player.showTitle(Text.of("<a>{} {} Spawned in {}!", count, count == 1 ? "Pest" : "Pests", plot), Text.of(""),
                Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(400)));
        if (lossNow > 0 && lossNow != lossBefore) {
            player.sendMessage(Text.of("<c>Farming Fortune has been reduced by {}%!", lossNow));
        }
    }

    private static int farmedPlotOrNeighbour(GardenProfile profile, int plot) {
        List<Integer> candidates = new ArrayList<>();
        candidates.add(plot);
        for (int neighbour : new TreeSet<>(GardenLayout.neighbours(plot))) {
            if (neighbour != GardenLayout.BARN && profile.isPlotUnlocked(neighbour)) candidates.add(neighbour);
        }
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    public static @Nullable PestCatalogue.Pest pickType(GardenProfile profile, int plot, long now) {
        int level = GardenProgress.level(profile);
        PestData.Spray spray = profile.pests().sprayOn(plot, now);
        List<PestCatalogue.Pest> pool = PestCatalogue.get().natural().stream().filter(pest -> pest.level() <= level).toList();
        if (pool.isEmpty()) return null;
        int[] weights = new int[pool.size()];
        int total = 0;
        for (int i = 0; i < pool.size(); i++) {
            weights[i] = PestRules.BASE_WEIGHT
                    + (spray != null && spray.material().equals(pool.get(i).spray()) ? PestRules.SPRAY_ATTRACT_WEIGHT : 0);
            total += weights[i];
        }
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (int i = 0; i < pool.size(); i++) {
            roll -= weights[i];
            if (roll < 0) return pool.get(i);
        }
        return pool.getLast();
    }

    public static void kill(SkyBlockPlayer player, PestEntity entity) {
        GardenProfile profile = Gardens.profile(player);
        PestData data = profile.pests();
        if (!data.removeAlive(entity.aliveId())) return;
        Instance instance = player.getInstance();
        if (instance != null) PestWorld.despawn(instance, entity.aliveId());
        data.addKill(entity.pest().id());
        data.setStored(data.getStored() + 1);
        PestRewards.award(player, profile, entity.pest());
        Gardens.save(player, profile);
    }

    public static void tick(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        PestData data = profile.pests();
        Instance instance = player.getInstance();
        if (instance == null) return;
        long now = System.currentTimeMillis();
        offlineSpawns(player, profile, data, now);
        data.setLastTick(now);
        PestTraps.tick(data, now);
        PestWorld.reconcile(instance, data);
        PestWorld.reconcileTraps(instance, data);
    }

    private static void offlineSpawns(SkyBlockPlayer player, GardenProfile profile, PestData data, long now) {
        if (!unlocked(profile) || data.getLastTick() == 0) return;
        long hours = (now - data.getLastTick()) / PestRules.OFFLINE_SPAWN_MILLIS;
        if (hours <= 0) return;
        List<Integer> plots = new ArrayList<>(profile.getUnlockedPlots());
        plots.removeIf(plot -> plot == GardenLayout.BARN || !profile.isPlotCleaned(plot));
        if (plots.isEmpty()) return;
        TreeSet<Integer> hit = new TreeSet<>();
        for (long i = 0; i < hours && data.aliveCount() < PestData.MAX_ALIVE; i++) {
            int plot = plots.get(ThreadLocalRandom.current().nextInt(plots.size()));
            PestCatalogue.Pest type = pickType(profile, plot, now);
            if (type == null) continue;
            data.getAlive().add(PestData.Alive.create(type.id(), plot));
            hit.add(plot);
        }
        if (hit.isEmpty()) return;
        Gardens.save(player, profile);
        player.sendMessage(Text.of("<6><l>GROSS!</l> <7>While you were offline, <2>Pests <7>spawned in <a>Plots {}<7>!",
                String.join("<7>, <a>", hit.stream().map(String::valueOf).toList())));
    }
}
