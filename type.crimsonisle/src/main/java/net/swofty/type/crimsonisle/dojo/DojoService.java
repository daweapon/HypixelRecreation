package net.swofty.type.crimsonisle.dojo;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoChallengeLock;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoDamageProtection;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoGrade;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoProgress;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTest;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTestCompletedEvent;
import net.swofty.type.skyblockgeneric.event.actions.player.ActionPlayerChangeHypixelMenuDisplay;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Runs Dojo challenges. The Arena is one real hall in the shared world, so one player uses it at a
 * time and anyone else who asks is queued and brought in as soon as it frees up.
 */
public final class DojoService {
    public static final String TELEPORT = "Ahhh, here we go! Let's get you into the Arena.";
    public static final String WAITING = "Have patience, I'm getting you in right now!";
    private static final int WARMUP_TICKS = 20;

    private static final Deque<Queued> QUEUE = new ArrayDeque<>();
    private static @Nullable Session active;
    private static boolean started;
    private static long serverTicks;

    private DojoService() {
    }

    public static void start() {
        if (started) return;
        started = true;
        DojoDamageProtection.register(DojoProtection::isProtected);
        MinecraftServer.getSchedulerManager().buildTask(DojoService::tick).repeat(TaskSchedule.tick(1)).schedule();
    }

    public static boolean isInChallenge(SkyBlockPlayer player) {
        Session session = active;
        return session != null && session.player.getUuid().equals(player.getUuid());
    }

    public static boolean isWaiting(SkyBlockPlayer player) {
        return QUEUE.stream().anyMatch(queued -> queued.player.getUuid().equals(player.getUuid()));
    }

    public static @Nullable DojoChallenge challengeOf(SkyBlockPlayer player) {
        Session session = active;
        return session != null && session.player.getUuid().equals(player.getUuid()) ? session.challenge : null;
    }

    public static void request(SkyBlockPlayer player, DojoTest test) {
        if (isInChallenge(player) || isWaiting(player)) {
            MasterTao.say(player, WAITING);
            return;
        }
        if (!DojoTests.isAvailable(test)) {
            MasterTao.say(player, DojoTests.DISABLED);
            return;
        }
        String problem = DojoRequirements.problem(player);
        if (problem != null) {
            MasterTao.say(player, problem);
            return;
        }
        if (!DojoProgress.isUnlocked(player, test)) return;
        player.closeInventory();
        if (active != null) {
            QUEUE.add(new Queued(player, test));
            MasterTao.say(player, WAITING);
            return;
        }
        begin(player, test);
    }

    private static void begin(SkyBlockPlayer player, DojoTest test) {
        MasterTao.say(player, TELEPORT);
        DojoChallenge challenge = DojoTests.create(test, player);
        active = new Session(player, challenge);
        DojoChallengeLock.lock(player, DojoService::cancel);
        ActionPlayerChangeHypixelMenuDisplay.setMainMenu(player);
        player.getPetData().updatePetEntityImpl(null);
        player.addEffect(new Potion(PotionEffect.BLINDNESS, 0, WARMUP_TICKS + 10));
    }

    public static void cancel(SkyBlockPlayer player) {
        Session session = active;
        if (session == null || !session.player.getUuid().equals(player.getUuid())) return;
        finish(session, true);
    }

    public static void disconnect(SkyBlockPlayer player) {
        DojoProtection.forget(player);
        QUEUE.removeIf(queued -> queued.player.getUuid().equals(player.getUuid()));
        Session session = active;
        if (session == null || !session.player.getUuid().equals(player.getUuid())) return;
        session.challenge.cleanup();
        DojoChallengeLock.unlock(player);
        DojoRequirements.clearInventory(player);
        player.setGameMode(session.previousMode);
        active = null;
    }

    private static void tick() {
        if (++serverTicks % 10 == 0) DojoProtection.update();
        Session session = active;
        if (session == null) {
            admitNext();
            return;
        }
        if (!session.player.isOnline()) {
            disconnect(session.player);
            return;
        }
        if (session.warmup > 0) {
            if (--session.warmup == 0) {
                session.player.teleport(session.challenge.begin());
                if (session.previousMode == GameMode.SURVIVAL) session.player.setGameMode(GameMode.ADVENTURE);
            }
            return;
        }
        try {
            session.challenge.update();
        } catch (RuntimeException exception) {
            Logger.error(exception, "Dojo challenge {} failed", session.challenge.getTest());
            session.challenge.fail();
        }
        if (session.challenge.ticks % 5 == 0) showProgress(session);
        if (session.challenge.isOver()) finish(session, false);
    }

    private static void admitNext() {
        while (!QUEUE.isEmpty()) {
            Queued next = QUEUE.poll();
            if (!next.player.isOnline()) continue;
            if (DojoRequirements.problem(next.player) != null) {
                MasterTao.say(next.player, DojoRequirements.problem(next.player));
                continue;
            }
            begin(next.player, next.test);
            return;
        }
    }

    private static void showProgress(Session session) {
        DojoChallenge challenge = session.challenge;
        DojoTest test = challenge.getTest();
        long millis = test.isTimed()
                ? Math.max(0, test.getTimeLimitSeconds() * 1000L - challenge.elapsedMillis())
                : challenge.elapsedMillis();
        String label = test.isTimed() ? "Time Left" : "Time";
        SkyBlockActionBar.getFor(session.player).addReplacement(SkyBlockActionBar.BarSection.DEFENSE,
                Text.of("<6>Points: <e>" + StringUtility.commaify(challenge.score()) + "  <7>" + label + ": <a>"
                        + DojoFormat.clock(millis)), 10, 5);
    }

    private static void finish(Session session, boolean cancelled) {
        active = null;
        SkyBlockPlayer player = session.player;
        DojoChallenge challenge = session.challenge;
        DojoTest test = challenge.getTest();
        challenge.cleanup();

        int score = cancelled ? 0 : Math.max(0, challenge.score());
        long time = test.isTimed()
                ? Math.min(challenge.elapsedMillis(), test.getTimeLimitSeconds() * 1000L)
                : challenge.elapsedMillis();
        DojoGrade grade = DojoGrade.of(score);
        boolean wasUnlocked = test.ordinal() + 1 < DojoTest.values().length
                && DojoProgress.isUnlocked(player, DojoTest.values()[test.ordinal() + 1]);
        boolean newBest = DojoProgress.record(player, test, score, time);

        DojoChallengeLock.unlock(player);
        DojoRequirements.clearInventory(player);
        ActionPlayerChangeHypixelMenuDisplay.setMainMenu(player);
        player.setGameMode(session.previousMode);
        player.setVelocity(Vec.ZERO);
        player.teleport(DojoArena.EXIT);
        player.getPetData().updatePetEntityImpl(player);

        DojoResults.announce(player, test, score, grade, time, cancelled, newBest, wasUnlocked);
        if (grade == DojoGrade.S) player.getAchievementHandler().completeAchievement("skyblock.dojo_master");
        HypixelEventHandler.callCustomEvent(new DojoTestCompletedEvent(player, test, score, grade));
    }

    private static final class Session {
        private final SkyBlockPlayer player;
        private final DojoChallenge challenge;
        private final GameMode previousMode;
        private int warmup = WARMUP_TICKS;

        private Session(SkyBlockPlayer player, DojoChallenge challenge) {
            this.player = player;
            this.challenge = challenge;
            this.previousMode = player.getGameMode();
        }
    }

    private record Queued(SkyBlockPlayer player, DojoTest test) {
    }
}
