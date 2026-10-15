package dev.daweapon.type.crimsonisle.quest.rescue;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestBoard;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestService;
import dev.daweapon.type.skyblockgeneric.crimson.quest.RescueStage;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The stealth half of a Rescue Mission. Guards watch a cone in front of them, sweeping side to side, and hear
 * players close by (sprinting carries furthest, sneaking barely at all). Ringing a bell turns nearby guards
 * toward it for a while. Being noticed gets you thrown back to the Undercover Agent with the mission still
 * running; leaving the venue abandons it until you talk to the agent again. Every number here is an estimate:
 * Hypixel documents the senses, not their ranges.
 */
public final class RescueService {
    static final double SIGHT_RANGE = 14;
    static final double SIGHT_HALF_ANGLE = 45;
    static final double SWEEP_DEGREES = 70;
    static final long SWEEP_PERIOD_MS = 10_000;
    static final double HEAR_SPRINTING = 10;
    static final double HEAR_WALKING = 5;
    static final double HEAR_SNEAKING = 1.5;
    static final double BELL_RADIUS = 12;
    static final long DISTRACTION_MS = 10_000;
    static final long GRACE_MS = 3_000;
    static final String RESTRICTED_LINE = "Hey, this is a restricted area. Leave now!";

    private static final Map<UUID, RescueState> STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> GRACE = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<RescueGuardNPC, Long>> DISTRACTED = new ConcurrentHashMap<>();
    private static final List<RescueGuardNPC> GUARDS = new ArrayList<>();

    private RescueService() {
    }

    public static void start() {
        Instance instance = HypixelConst.getInstanceContainer();
        for (RescueSite site : RescueSite.values()) {
            new RescueRecruiterNPC(site).register();
            new UndercoverAgentNPC(site).register();
            new HostageNPC(site).register();
            for (int i = 0; i < site.getGuardPosts().size(); i++) {
                RescueGuardNPC guard = new RescueGuardNPC(site, i);
                guard.register();
                GUARDS.add(guard);
            }
            site.getBells().forEach(bell -> instance.loadChunk(bell).thenRun(() -> instance.setBlock(bell, Block.BELL)));
        }
        MinecraftServer.getSchedulerManager().buildTask(RescueService::refreshAll).repeat(TaskSchedule.seconds(1)).schedule();
        MinecraftServer.getSchedulerManager().buildTask(RescueService::tick).repeat(TaskSchedule.tick(4)).schedule();
    }

    public static @Nullable RescueState state(SkyBlockPlayer player) {
        return STATES.get(player.getUuid());
    }

    public static void refresh(SkyBlockPlayer player) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        FactionQuestBoard.Slot slot = faction == null ? null : FactionQuestService.activeRescue(player);
        if (slot == null) {
            STATES.remove(player.getUuid());
            return;
        }
        STATES.put(player.getUuid(), new RescueState(RescueSite.of(faction), slot.quest().tier(), slot.getProgress()));
    }

    public static void setStage(SkyBlockPlayer player, int stage) {
        FactionQuestService.setRescueStage(player, stage);
        refresh(player);
    }

    public static void begin(SkyBlockPlayer player) {
        setStage(player, RescueStage.INFILTRATING);
        GRACE.put(player.getUuid(), System.currentTimeMillis() + GRACE_MS);
    }

    public static void abandon(SkyBlockPlayer player, @Nullable String message) {
        setStage(player, RescueStage.RECRUITED);
        DISTRACTED.remove(player.getUuid());
        if (message != null) player.sendMessage(message);
    }

    public static void rescue(SkyBlockPlayer player, HostageNPC hostage) {
        RescueState state = state(player);
        if (state == null || !state.infiltrating(hostage.getSite())) return;
        hostage.sendNPCMessage(player, "Thank you! I'll find my own way home from here.");
        FactionQuestService.completeRescue(player);
        DISTRACTED.remove(player.getUuid());
        refresh(player);
    }

    /** A rung bell draws the attention of every guard near it, for this player's mission only. */
    public static boolean ringBell(SkyBlockPlayer player, Point bell) {
        RescueState state = state(player);
        if (state == null || state.stage() != RescueStage.INFILTRATING || !state.site().contains(bell)) return false;
        long until = System.currentTimeMillis() + DISTRACTION_MS;
        Map<RescueGuardNPC, Long> distracted = DISTRACTED.computeIfAbsent(player.getUuid(), _ -> new ConcurrentHashMap<>());
        int count = 0;
        for (RescueGuardNPC guard : GUARDS) {
            if (guard.getSite() != state.site() || guard.post().distance(bell) > BELL_RADIUS) continue;
            distracted.put(guard, until);
            count++;
        }
        player.sendMessage(count == 0 ? "<7>The bell rings out, but no guard is close enough to hear it."
                : "<e>The ringing draws the attention of " + count + " nearby guard" + (count == 1 ? "" : "s") + "!");
        return true;
    }

    public static void forget(SkyBlockPlayer player) {
        STATES.remove(player.getUuid());
        GRACE.remove(player.getUuid());
        DISTRACTED.remove(player.getUuid());
    }

    private static void refreshAll() {
        SkyBlockGenericLoader.getLoadedPlayers().forEach(RescueService::refresh);
    }

    private static void tick() {
        long now = System.currentTimeMillis();
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            RescueState state = state(player);
            if (state == null || state.stage() != RescueStage.INFILTRATING) continue;
            RescueSite site = state.site();
            if (!site.contains(player.getPosition())) {
                abandon(player, "<c>You left " + site.getVenueName() + "<c>, so the rescue was abandoned! "
                        + "<7>Talk to the Undercover Agent to try again.");
                continue;
            }
            Map<RescueGuardNPC, Long> distracted = DISTRACTED.getOrDefault(player.getUuid(), Map.of());
            boolean graced = GRACE.getOrDefault(player.getUuid(), 0L) > now;
            for (RescueGuardNPC guard : GUARDS) {
                if (guard.getSite() != site) continue;
                Pos bell = distracted.getOrDefault(guard, 0L) > now ? nearestBell(site, guard.post()) : null;
                float yaw = bell != null ? yawTowards(guard.post(), bell) : sweepYaw(site, guard, now);
                turn(player, guard, yaw);
                if (bell == null && !graced && notices(player, guard.post(), yaw)) {
                    thrownOut(player, guard);
                    break;
                }
            }
        }
    }

    static float sweepYaw(RescueSite site, RescueGuardNPC guard, long now) {
        double phase = (now % SWEEP_PERIOD_MS) / (double) SWEEP_PERIOD_MS * Math.PI * 2 + guard.getIndex() * 1.3;
        return (float) (site.guardYaw(guard.post()) + Math.sin(phase) * SWEEP_DEGREES);
    }

    static float yawTowards(Point from, Point to) {
        return (float) Math.toDegrees(-Math.atan2(to.x() - from.x(), to.z() - from.z()));
    }

    static double hearingRange(SkyBlockPlayer player) {
        if (player.isSneaking()) return HEAR_SNEAKING;
        return player.isSprinting() ? HEAR_SPRINTING : HEAR_WALKING;
    }

    private static boolean notices(SkyBlockPlayer player, Pos post, float yaw) {
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec guardEye = post.asVec().add(0, 1.6, 0);
        double distance = guardEye.distance(eye);
        if (distance <= hearingRange(player)) return true;
        if (distance > SIGHT_RANGE) return false;
        if (!withinCone(guardEye, yaw, eye)) return false;
        return clearLine(player.getInstance(), guardEye, eye.asVec());
    }

    static boolean withinCone(Point guardEye, float yaw, Point target) {
        double angle = yawTowards(guardEye, target) - yaw;
        angle = ((angle % 360) + 540) % 360 - 180;
        return Math.abs(angle) <= SIGHT_HALF_ANGLE;
    }

    private static boolean clearLine(Instance instance, Vec from, Vec to) {
        Vec step = to.sub(from);
        double length = step.length();
        Vec direction = step.normalize().mul(0.4);
        Vec point = from;
        for (double travelled = 0.4; travelled < length; travelled += 0.4) {
            point = point.add(direction);
            if (instance.getBlock(point).isSolid()) return false;
        }
        return true;
    }

    private static void thrownOut(SkyBlockPlayer player, RescueGuardNPC guard) {
        RescueSite site = guard.getSite();
        guard.sendNPCMessage(player, RESTRICTED_LINE);
        player.teleport(site.getAgent());
        player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.NEUTRAL, 1f, 1f));
        player.sendMessage("<c>You were spotted and thrown out! <7>Sneak past the guards' sight to reach the hostage.");
        GRACE.put(player.getUuid(), System.currentTimeMillis() + GRACE_MS);
        DISTRACTED.remove(player.getUuid());
    }

    private static Pos nearestBell(RescueSite site, Pos post) {
        Pos best = site.getBells().getFirst();
        for (Pos bell : site.getBells()) if (bell.distance(post) < best.distance(post)) best = bell;
        return best.add(0.5, 0, 0.5);
    }

    private static void turn(SkyBlockPlayer player, RescueGuardNPC guard, float yaw) {
        HypixelNPC.PlayerNPCCache cache = HypixelNPC.getPerPlayerNPCs().get(player.getUuid());
        if (cache == null) return;
        Entity entity = cache.get(guard);
        if (entity != null) entity.setView(yaw, 0);
    }
}
