package net.swofty.type.jerrysworkshop.defense;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Runs the "Defend Jerry's Workshop" wave-defense minigame (Roadmap #20).
//
// Real Hypixel (SkyBlock Wiki, "Jerry's Workshop"): the event is 5 rounds with an 8-minute timer
// between them; the real mob roster is Frozen Steve/Frosty/Grinch/Nutcracker/Yeti/Reindrake
// (see WorkshopMobVariant), damageable only by snowballs (see WorkshopDefenseMob/
// SnowballEntityImpl). Rewards (gifts, scaled by damage dealt) are handed out at the end of each
// round. The wiki's "8 minutes in total" between-round phrasing is ambiguous about whether that's
// the prep timer or something else - this treats it as the full prep/break duration before each
// wave, which is the reading that makes the rest of the mechanic (players regroup, sell gifts,
// prepare) make sense.
//
// Scope cuts made explicit rather than silently guessed at:
//  - Real Hypixel: mobs attack 5 gift piles scattered around the village, and piles can be lost
//    if not defended. This is modeled as a single circular arena instead - simpler, but a real
//    divergence from "defend 5 separate piles."
//  - Runs in-process on this single server type (Jerry's Workshop is its own dedicated server),
//    not cross-server broadcast like Dark Auction - there's only ever one workshop instance.
public final class JerryWorkshopDefenseManager {

    public enum Phase {IDLE, PREPARING, WAVE_ACTIVE}

    private static final int TOTAL_ROUNDS = 5;
    private static final long WAVE_DURATION_MS = 90_000L;
    private static final long BETWEEN_ROUNDS_MS = 8L * 60 * 1000;
    private static final int SNOWBALLS_GRANTED_PER_WAVE = 32;

    // Approximate arena center, inferred from the existing NPC/prop coordinates already placed in
    // this server type (St. Jerry (-22.5,76,92.5), Sherry (6.5,76,95.5), GlassDisplay props at
    // (13-16,77,91-101)) - these NPC positions are now wiki-confirmed exact, but no exact map
    // reference for the actual battle floor itself was available.
    private static final Pos ARENA_CENTER = new Pos(-4.5, 76, 96.5);
    private static final double ARENA_RADIUS = 18;

    private static volatile Phase phase = Phase.IDLE;
    private static int currentRound = 0;
    private static long phaseEndsAtMillis = 0;
    private static boolean tickerStarted = false;
    private static boolean triggeredThisOccurrence = false;

    private static final Map<UUID, Double> damageDealt = new ConcurrentHashMap<>();
    private static final List<SkyBlockMob> activeMobs = new ArrayList<>();

    private JerryWorkshopDefenseManager() {
    }

    public static synchronized void beginEvent() {
        if (phase != Phase.IDLE) {
            Logger.info("Jerry's Workshop defense already running, ignoring duplicate start");
            return;
        }
        if (HypixelConst.getInstanceContainer() == null) return;

        currentRound = 0;
        damageDealt.clear();
        broadcast("<c><l>JERRY'S WORKSHOP IS UNDER ATTACK! <7>Defend it over 5 rounds with your Snowballs for gifts!");
        startPreparing();
        ensureTicker();
    }

    // Polled periodically by TypeJerrysWorkshopLoader while the server is up - auto-starts the
    // defense once per Season of Jerry occurrence, matching real Hypixel's automatic trigger,
    // without needing a separate cross-server scheduler like Dark Auction's (this server type is
    // a single dedicated instance, so an in-process poll is enough).
    public static synchronized void checkAndAutoStart() {
        boolean active = SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.SEASON_OF_JERRY);
        if (!active) {
            triggeredThisOccurrence = false;
            return;
        }
        if (triggeredThisOccurrence || phase != Phase.IDLE) return;
        triggeredThisOccurrence = true;
        beginEvent();
    }

    public static Phase getPhase() {
        return phase;
    }

    public static int getCurrentRound() {
        return currentRound;
    }

    public static long getPhaseRemainingMillis() {
        return Math.max(0, phaseEndsAtMillis - System.currentTimeMillis());
    }

    public static void recordDamage(UUID playerUuid, double amount) {
        if (phase != Phase.WAVE_ACTIVE) return;
        damageDealt.merge(playerUuid, amount, Double::sum);
    }

    private static void ensureTicker() {
        if (tickerStarted) return;
        tickerStarted = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            tick();
            return phase == Phase.IDLE ? TaskSchedule.stop() : TaskSchedule.seconds(1);
        });
    }

    private static void tick() {
        if (phase == Phase.PREPARING && System.currentTimeMillis() >= phaseEndsAtMillis) {
            startWave();
        } else if (phase == Phase.WAVE_ACTIVE) {
            boolean cleared = activeMobs.stream().allMatch(SkyBlockMob::isRemoved);
            if (cleared || System.currentTimeMillis() >= phaseEndsAtMillis) {
                endWave();
            }
        }
    }

    private static void startPreparing() {
        phase = Phase.PREPARING;
        phaseEndsAtMillis = System.currentTimeMillis() + BETWEEN_ROUNDS_MS;
        broadcast("<e>Round " + (currentRound + 1) + " begins in 8 minutes - prepare yourselves!");
    }

    private static void startWave() {
        currentRound++;
        phase = Phase.WAVE_ACTIVE;
        phaseEndsAtMillis = System.currentTimeMillis() + WAVE_DURATION_MS;
        damageDealt.clear();
        spawnWave();
        grantSnowballs();
        broadcast("<c><l>ROUND " + currentRound + " HAS BEGUN! <7>Pelt them with Snowballs!");
    }

    private static void spawnWave() {
        activeMobs.clear();
        WorkshopMobVariant[] variants = WorkshopMobVariant.values();
        int mobCount = 2 + currentRound;
        for (int i = 0; i < mobCount; i++) {
            Pos spawnPos = randomArenaPosition();
            WorkshopMobVariant variant = variants[(int) (Math.random() * variants.length)];
            WorkshopDefenseMob mob = new WorkshopDefenseMob(variant, currentRound);
            mob.setInstance(HypixelConst.getInstanceContainer(), spawnPos);
            activeMobs.add(mob);
        }
    }

    private static void grantSnowballs() {
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            player.getInventory().addItemStack(net.minestom.server.item.ItemStack.of(net.minestom.server.item.Material.SNOWBALL, SNOWBALLS_GRANTED_PER_WAVE));
        }
    }

    private static Pos randomArenaPosition() {
        double angle = Math.random() * Math.PI * 2;
        double distance = Math.random() * ARENA_RADIUS;
        double x = ARENA_CENTER.x() + Math.cos(angle) * distance;
        double z = ARENA_CENTER.z() + Math.sin(angle) * distance;
        return new Pos(x, ARENA_CENTER.y(), z);
    }

    private static void endWave() {
        for (SkyBlockMob mob : activeMobs) {
            if (!mob.isRemoved()) mob.remove();
        }
        activeMobs.clear();

        distributeRewards();

        if (currentRound >= TOTAL_ROUNDS) {
            phase = Phase.IDLE;
            tickerStarted = false;
            broadcast("<a><l>JERRY'S WORKSHOP HAS BEEN DEFENDED! <7>Thanks for defending all 5 rounds!");
        } else {
            startPreparing();
        }
    }

    private static void distributeRewards() {
        if (damageDealt.isEmpty()) return;

        List<Map.Entry<UUID, Double>> ranked = damageDealt.entrySet().stream()
                .sorted(Comparator.comparingDouble((Map.Entry<UUID, Double> e) -> e.getValue()).reversed())
                .toList();

        for (int i = 0; i < ranked.size(); i++) {
            UUID uuid = ranked.get(i).getKey();
            SkyBlockPlayer player = findLoadedPlayer(uuid);
            if (player == null) continue;

            // Top-3 damage dealers get a Red Gift, everyone who dealt damage gets a Green Gift -
            // matches the wiki's "gifts depending on the amount of damage done" without a sourced
            // exact formula.
            boolean topDamage = i < 3;
            player.addAndUpdateItem(new SkyBlockItem(
                    topDamage ? net.swofty.commons.skyblock.item.ItemType.RED_GIFT
                            : net.swofty.commons.skyblock.item.ItemType.GREEN_GIFT));
            player.sendMessage("<a>You received a {} <a>for your efforts in Round " + currentRound + "!",
                    topDamage ? "<c>Red Gift" : "<a>Green Gift");
        }
    }

    private static SkyBlockPlayer findLoadedPlayer(UUID uuid) {
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (player.getUuid().equals(uuid)) return player;
        }
        return null;
    }

    private static void broadcast(String markup) {
        Text text = Text.parseLenient(markup);
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            player.sendMessage(text.asComponent());
        }
    }
}
