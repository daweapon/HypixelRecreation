package dev.daweapon.type.skyblockgeneric.hotf.throwing;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.event.actions.player.region.ActionRegionBlockBreak;
import dev.daweapon.type.skyblockgeneric.foraging.ForagingTreeManager;
import dev.daweapon.type.skyblockgeneric.hotf.HotfAbilities;
import dev.daweapon.type.skyblockgeneric.hotf.HotfAbility;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class AxeThrowing {
    public static final double SWEEP_SCALE_WITH_PENALTY = 0.5;
    public static final double SWEEP_SCALE_WITHOUT_PENALTY = 1.0;
    public static final int RANGE = 20;
    public static final long THROW_COOLDOWN_MILLIS = 1000;
    public static final int HOMING_RADIUS = 8;
    public static final double HOMING_MINIMUM_FACING = 0.2;
    public static final int RICOCHET_RADIUS = 10;
    public static final int RICOCHET_MINIMUM_DISTANCE = 3;
    public static final int MAX_RICOCHETS = 3;
    public static final String HOMING_PERK = "homing_axe";
    public static final String RICOCHET_PERK = "ricochet";

    private static final Map<UUID, Long> NEXT_THROW = new ConcurrentHashMap<>();

    private AxeThrowing() {
    }

    public static double sweepScale(SkyBlockPlayer player) {
        return HotfAbilities.isActive(player, HotfAbility.AXE_TOSS) ? SWEEP_SCALE_WITHOUT_PENALTY : SWEEP_SCALE_WITH_PENALTY;
    }

    public static boolean tossWindowOpen(SkyBlockPlayer player) {
        return HotfAbilities.isActive(player, HotfAbility.AXE_TOSS);
    }

    public static boolean throwWithCooldown(SkyBlockPlayer player, SkyBlockItem item, @Nullable Point clicked) {
        long now = System.currentTimeMillis();
        if (NEXT_THROW.getOrDefault(player.getUuid(), 0L) > now) return false;
        if (!launch(player, item, clicked)) return false;
        NEXT_THROW.put(player.getUuid(), now + THROW_COOLDOWN_MILLIS);
        return true;
    }

    public static boolean launch(SkyBlockPlayer player, SkyBlockItem item, @Nullable Point clicked) {
        Instance instance = player.getInstance();
        if (HypixelConst.isIslandServer() || instance == null) return false;
        Point target = resolveTarget(player, instance, clicked);
        if (target == null) return false;
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Pos start = eye.add(player.getPosition().direction().mul(0.6));
        fly(player, instance, item, start, target, sweepScale(player), 0);
        return true;
    }

    public static void clear(UUID player) {
        NEXT_THROW.remove(player);
    }

    private static @Nullable Point resolveTarget(SkyBlockPlayer player, Instance instance, @Nullable Point clicked) {
        Point aimed = clicked != null ? clicked : player.getTargetBlockPosition(RANGE);
        if (aimed != null && isLog(instance, aimed)) return aimed;
        if (!HotfService.onForagingIsland() || HotfService.data(player).activeLevel(HOMING_PERK) <= 0) return null;
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec look = player.getPosition().direction();
        Point center = aimed != null ? aimed : eye.add(look.mul(RANGE));
        return ThrowTargeting.nearestLog(center, HOMING_RADIUS, 0,
                point -> isLog(instance, point) && inFrontAndInRange(eye, look, point), point -> false);
    }

    private static boolean inFrontAndInRange(Pos eye, Vec look, Point point) {
        Vec toPoint = Vec.fromPoint(point).add(0.5, 0.5, 0.5).sub(eye);
        double distance = toPoint.length();
        return distance <= RANGE + HOMING_RADIUS && toPoint.normalize().dot(look) >= HOMING_MINIMUM_FACING;
    }

    private static boolean isLog(Instance instance, Point point) {
        return ForagingTreeManager.isTreeLog(instance.getBlock(point));
    }

    private static void fly(SkyBlockPlayer player, Instance instance, SkyBlockItem item, Pos from, Point target,
                            double sweepScale, int ricochets) {
        Pos center = new Pos(target.blockX() + 0.5, target.blockY() + 0.5, target.blockZ() + 0.5);
        ThrownAxeEntity.launch(instance, from, center, item.getItemStack(), () -> {
            if (!player.isOnline() || player.getInstance() != instance) return;
            Set<Point> cut = new HashSet<>();
            cut.add(new BlockVec(target));
            land(player, instance, item, target, sweepScale);
            ricochet(player, instance, item, center, target, sweepScale, ricochets, cut);
        });
    }

    private static void land(SkyBlockPlayer player, Instance instance, SkyBlockItem item, Point target, double sweepScale) {
        if (!isLog(instance, target)) return;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(target);
        if (region == null) return;
        ActionRegionBlockBreak.harvestTree(player, item, region, target, sweepScale);
    }

    private static void ricochet(SkyBlockPlayer player, Instance instance, SkyBlockItem item, Pos from, Point landed,
                                 double sweepScale, int ricochets, Set<Point> visited) {
        if (ricochets >= MAX_RICOCHETS) return;
        double chance = HotfService.perkValue(player, RICOCHET_PERK);
        if (!ThrowTargeting.bounces(chance, ThreadLocalRandom.current().nextDouble())) return;
        Point next = ThrowTargeting.nearestLog(landed, RICOCHET_RADIUS, RICOCHET_MINIMUM_DISTANCE,
                point -> isLog(instance, point), visited::contains);
        if (next == null) return;
        visited.add(new BlockVec(next));
        Pos center = new Pos(next.blockX() + 0.5, next.blockY() + 0.5, next.blockZ() + 0.5);
        ThrownAxeEntity.launch(instance, from, center, item.getItemStack(), () -> {
            if (!player.isOnline() || player.getInstance() != instance) return;
            land(player, instance, item, next, sweepScale);
            ricochet(player, instance, item, center, next, sweepScale, ricochets + 1, visited);
        });
    }
}
